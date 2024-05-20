package com.infinum.sentinel.data.models.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.infinum.sentinel.utils.ChronoUnit

@Entity(tableName = "certificate_monitor")
internal data class CertificateMonitorEntity(

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    var id: Long?,

    @ColumnInfo(name = "run_on_start", defaultValue = "false")
    var runOnStart: Boolean = false,

    @ColumnInfo(name = "run_in_background", defaultValue = "false")
    var runInBackground: Boolean = false,

    @ColumnInfo(name = "notify_invalid_now", defaultValue = "false")
    var notifyInvalidNow: Boolean = false,

    @ColumnInfo(name = "notify_to_expire", defaultValue = "false")
    var notifyToExpire: Boolean = false,

    @ColumnInfo(name = "expire_in_amount", defaultValue = "0")
    var expireInAmount: Int = 0,

    @ColumnInfo(name = "expire_in_unit", defaultValue = "DAYS")
    var expireInUnit: ChronoUnit = ChronoUnit.DAYS
)
