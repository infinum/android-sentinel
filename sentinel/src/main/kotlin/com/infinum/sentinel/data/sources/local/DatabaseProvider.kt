package com.infinum.sentinel.data.sources.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.infinum.sentinel.data.sources.local.room.SentinelDatabase

internal object DatabaseProvider {

    private lateinit var sentinelDatabase: SentinelDatabase

    fun initialise(context: Context): DatabaseProvider {
        sentinelDatabase = Room.databaseBuilder(
            context.applicationContext,
            SentinelDatabase::class.java,
            databaseName(context)
        )
            .allowMainThreadQueries()
            .createFromAsset("databases/sentinel_default.db")
            .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
            .fallbackToDestructiveMigration()
            .build()

        return this
    }

    fun sentinel() = lazyOf(sentinelDatabase).value

    private fun databaseName(context: Context) =
        "sentinel_" + context.applicationContext.packageName.replace(".", "_")
            .toLowerCase() + ".db"
}
