package com.infinum.sentinel.data.sources.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.infinum.sentinel.data.models.local.FormatEntity
import com.infinum.sentinel.data.models.local.TriggerEntity
import com.infinum.sentinel.data.sources.local.room.dao.FormatsDao
import com.infinum.sentinel.data.sources.local.room.dao.TriggersDao
import com.infinum.sentinel.data.sources.local.room.typeconverters.FormatTypeConverter
import com.infinum.sentinel.data.sources.local.room.typeconverters.TriggerTypeConverter

@Database(entities = [TriggerEntity::class, FormatEntity::class], version = 1, exportSchema = false)
@TypeConverters(value = [TriggerTypeConverter::class, FormatTypeConverter::class])
internal abstract class SentinelDatabase : RoomDatabase() {

    companion object {

        fun create(context: Context): SentinelDatabase =
            Room.databaseBuilder(
                context.applicationContext,
                SentinelDatabase::class.java,
                databaseName(context)
            )
                .allowMainThreadQueries()
                .createFromAsset("databases/sentinel_default.db")
                .setJournalMode(JournalMode.TRUNCATE)
                .fallbackToDestructiveMigration()
                .build()

        private fun databaseName(context: Context) =
            "sentinel_" + context.applicationContext.packageName.replace(".", "_")
                .toLowerCase() + ".db"
    }

    abstract fun triggersDao(): TriggersDao

    abstract fun formatsDao(): FormatsDao
}
