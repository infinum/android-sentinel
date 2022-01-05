package com.infinum.sentinel.data.sources.local.room.typeconverters

import androidx.room.TypeConverter
import com.infinum.sentinel.data.models.local.crash.CrashData
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal class CrashDataConverter {

    @TypeConverter
    fun toEntity(data: String): CrashData =
        Json.decodeFromString<CrashData>(data)

    @TypeConverter
    fun fromEntity(data: CrashData): String =
        Json.encodeToString(data)
}
