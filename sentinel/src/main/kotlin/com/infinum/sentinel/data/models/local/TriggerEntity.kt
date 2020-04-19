package com.infinum.sentinel.data.models.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.infinum.sentinel.data.models.memory.triggers.TriggerType

@Entity(tableName = "triggers")
data class TriggerEntity(

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    var id: Long?,

    @ColumnInfo(name = "type")
    var type: TriggerType?,

    @ColumnInfo(name = "enabled", defaultValue = "true")
    var enabled: Boolean = true,

    @ColumnInfo(name = "editable", defaultValue = "true")
    var editable: Boolean = true
)
