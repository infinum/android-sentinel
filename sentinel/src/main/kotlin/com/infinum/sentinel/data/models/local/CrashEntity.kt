package com.infinum.sentinel.data.models.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.infinum.sentinel.data.models.local.crash.CrashData

@Entity(tableName = "crashes")
internal data class CrashEntity(

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long? = null,

    @ColumnInfo(name = "application_name")
    var applicationName: String,

    @ColumnInfo(name = "timestamp")
    var timestamp: Long,

    @ColumnInfo(name = "data")
    var data: CrashData
)
