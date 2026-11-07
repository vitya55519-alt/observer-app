package ru.observer.app.data

import android.content.Context
import android.content.SharedPreferences
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import ru.observer.app.BuildConfig

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val kind: String,
    val app: String = "",
    val contact: String = "",
    val title: String = "",
    val body: String = "",
    val ts: Long = System.currentTimeMillis(),
    val src: String = "",
    val synced: Boolean = false
)

@Dao
interface Dao {
    @Insert suspend fun add(e: Event): Long
    @Query("SELECT * FROM events WHERE synced = 0 LIMIT 100") suspend fun unsynced(): List<Event>
    @Query("UPDATE events SET synced = 1 WHERE id IN (:ids)") suspend fun mark(ids: List<Long>)
}

@Database(entities = [Event::class], version = 1, exportSchema = false)
abstract class Db : RoomDatabase() {
    abstract fun dao(): Dao
    companion object {
        @Volatile private var i: Db? = null
        fun get(c: Context): Db = i ?: synchronized(this) {
            i ?: Room.databaseBuilder(c.applicationContext, Db::class.java, "obs.db")
                .fallbackToDestructiveMigration().build().also { i = it }
        }
    }
}

object Prefs {
    private lateinit var p: SharedPreferences
    fun init(c: Context) { p = c.getSharedPreferences("obs", Context.MODE_PRIVATE) }
    var base: String
        get() = p.getString("base", BuildConfig.BASE) ?: BuildConfig.BASE
        set(v) { p.edit().putString("base", v.trim().trimEnd('/')).apply() }
    var token: String
        get() = p.getString("tok", "") ?: ""
        set(v) { p.edit().putString("tok", v).apply() }
    val enrolled: Boolean get() = token.isNotEmpty()
}
