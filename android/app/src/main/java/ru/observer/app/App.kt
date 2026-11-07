package ru.observer.app

import android.app.Application
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.provider.CallLog
import android.provider.Telephony
import ru.observer.app.collect.CallObserver
import ru.observer.app.collect.SmsObserver
import ru.observer.app.data.Prefs

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        Prefs.init(this)
        
        // Register SMS observer
        contentResolver.registerContentObserver(
            Telephony.Sms.CONTENT_URI,
            true,
            SmsObserver(this, Handler(Looper.getMainLooper()))
        )
        
        // Register Call Log observer
        contentResolver.registerContentObserver(
            CallLog.Calls.CONTENT_URI,
            true,
            CallObserver(this, Handler(Looper.getMainLooper()))
        )
    }
}
