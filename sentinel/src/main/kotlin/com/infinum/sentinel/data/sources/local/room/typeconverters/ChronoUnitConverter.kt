package com.infinum.sentinel.data.sources.local.room.typeconverters

import androidx.room.TypeConverter
import java.time.temporal.ChronoUnit

internal class ChronoUnitConverter {
    @TypeConverter
    fun toEnum(name: String): ChronoUnit = ChronoUnit.values().single { it.name == name }

    @TypeConverter
    fun fromEnum(unit: ChronoUnit): String = unit.name
}
