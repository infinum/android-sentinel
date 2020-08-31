package com.infinum.sentinel.data.models.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.infinum.sentinel.data.models.memory.formats.FormatType

@Entity(tableName = "formats")
internal data class FormatEntity(

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    var id: Long?,

    @ColumnInfo(name = "type")
    var type: FormatType?,

    @ColumnInfo(name = "selected", defaultValue = "false")
    var selected: Boolean = false
)
