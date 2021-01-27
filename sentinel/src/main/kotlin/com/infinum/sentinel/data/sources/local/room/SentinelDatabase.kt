package com.infinum.sentinel.data.sources.local.room

import androidx.room.Database
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

    abstract fun triggers(): TriggersDao

    abstract fun formats(): FormatsDao
}
