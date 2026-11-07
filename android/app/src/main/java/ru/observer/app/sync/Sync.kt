package ru.observer.app.sync

import android.content.Context
import androidx.work.*
import kotlinx.coroutines.runBlocking
import ru.observer.app.data.Dao
import ru.observer.app.data.Db
import ru.observer.app.net.Api
import ru.observer.app.net.Ws
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

object Sync {
    fun start(c: Context) {
        val req = PeriodicWorkRequestBuilder<W>(15, TimeUnit.MINUTES).build()
        WorkManager.getInstance(c).enqueueUniquePeriodicWork("sync", ExistingPeriodicWorkPolicy.UPDATE, req)
    }

    fun now(c: Context) {
        WorkManager.getInstance(c).enqueue(OneTimeWorkRequestBuilder<W>().setConstraints(
            Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()).build())
    }

    class W(c: Context, p: WorkerParameters) : Worker(c, p) {
        override fun doWork(): Result {
            val ctx = applicationContext
            Ws.start(ctx)
            val dao: Dao = Db.get(ctx).dao()
            val list = runBlocking { dao.unsynced() }
            if (list.isEmpty()) return Result.success()
            val latch = CountDownLatch(1)
            var ok = false
            Api.push(ctx, list) { r -> ok = r; latch.countDown() }
            latch.await(30, TimeUnit.SECONDS)
            if (ok) runBlocking { dao.mark(list.map { it.id }) }
            return Result.success()
        }
    }
}
