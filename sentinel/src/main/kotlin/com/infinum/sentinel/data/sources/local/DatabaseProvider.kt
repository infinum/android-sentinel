package com.infinum.sentinel.data.sources.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.infinum.sentinel.data.sources.local.room.SentinelDatabase

internal class DatabaseProvider(context: Context) {

    private val sentinelDatabase: SentinelDatabase = Room.databaseBuilder(
        context,
        SentinelDatabase::class.java,
        databaseName(context)
    )
        .allowMainThreadQueries()
        .createFromAsset("databases/sentinel_default.db")
        .setJournalMode(RoomDatabase.JournalMode.TRUNCATE)
        .fallbackToDestructiveMigration()
        .build()

    fun sentinel() = lazyOf(sentinelDatabase).value

    private fun databaseName(context: Context) =
        "sentinel_" + context.applicationContext.packageName.replace(".", "_")
            .toLowerCase() + ".db"
}
