package com.infinum.sentinel.data.sources.local.room.typeconverters

import androidx.room.TypeConverter
import com.infinum.sentinel.data.models.memory.formats.FormatType

internal class FormatTypeConverter {

    @TypeConverter
    fun toEnum(name: String): FormatType = FormatType.valueOf(name)

    @TypeConverter
    fun fromEnum(type: FormatType): String = type.name
}
