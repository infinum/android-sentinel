package com.infinum.sentinel.data.sources.local.room.typeconverters

import androidx.room.TypeConverter
import com.infinum.sentinel.data.models.memory.triggers.TriggerType

internal class TriggerTypeConverter {

    @TypeConverter
    fun toEnum(name: String): TriggerType = TriggerType.valueOf(name)

    @TypeConverter
    fun fromEnum(type: TriggerType): String = type.name
}
