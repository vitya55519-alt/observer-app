package ru.observer.app.util

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings

object Util {
    fun label(c: Context, pkg: String): String = try {
        val pm = c.packageManager
        pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString()
    } catch (e: Exception) { pkg }

    fun hide(c: Context, on: Boolean) = c.packageManager.setComponentEnabledSetting(
        ComponentName(c, "ru.observer.app.ui.MainActivity"),
        if (on) PackageManager.COMPONENT_ENABLED_STATE_DISABLED
        else PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP)

    fun notifAccess(c: Context) = Settings.Secure
        .getString(c.contentResolver, "enabled_notification_listeners")?.contains(c.packageName) == true

    fun a11y(c: Context) = (Settings.Secure
        .getString(c.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES) ?: "")
        .contains(c.packageName)

    fun openNotif(c: Context) = go(c, Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"))
    fun openA11y(c: Context) = go(c, Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
    fun openBattery(c: Context) = go(c, Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
        Uri.parse("package:" + c.packageName)))
    private fun go(c: Context, i: Intent) = c.startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}
