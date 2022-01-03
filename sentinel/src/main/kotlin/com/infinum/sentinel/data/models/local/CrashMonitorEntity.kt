package com.infinum.sentinel.data.models.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "crash_monitor")
internal data class CrashMonitorEntity(

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    var id: Long?,

    @ColumnInfo(name = "notify_exceptions", defaultValue = "false")
    var notifyExceptions: Boolean = false,

    @ColumnInfo(name = "notify_anrs", defaultValue = "false")
    var notifyAnrs: Boolean = false,
)
