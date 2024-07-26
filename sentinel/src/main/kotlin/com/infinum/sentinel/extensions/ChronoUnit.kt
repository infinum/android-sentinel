package com.infinum.sentinel.extensions

import android.os.Build
import androidx.annotation.RequiresApi
import java.time.temporal.ChronoUnit

@RequiresApi(Build.VERSION_CODES.O)
@Suppress("CyclomaticComplexMethod")
internal fun ChronoUnit.toSentinelChronoUnit(): com.infinum.sentinel.utils.ChronoUnit =
    when (this) {
        ChronoUnit.NANOS -> com.infinum.sentinel.utils.ChronoUnit.NANOS
        ChronoUnit.MICROS -> com.infinum.sentinel.utils.ChronoUnit.MICROS
        ChronoUnit.MILLIS -> com.infinum.sentinel.utils.ChronoUnit.MILLIS
        ChronoUnit.SECONDS -> com.infinum.sentinel.utils.ChronoUnit.SECONDS
        ChronoUnit.MINUTES -> com.infinum.sentinel.utils.ChronoUnit.MINUTES
        ChronoUnit.HOURS -> com.infinum.sentinel.utils.ChronoUnit.HOURS
        ChronoUnit.HALF_DAYS -> com.infinum.sentinel.utils.ChronoUnit.HALF_DAYS
        ChronoUnit.DAYS -> com.infinum.sentinel.utils.ChronoUnit.DAYS
        ChronoUnit.WEEKS -> com.infinum.sentinel.utils.ChronoUnit.WEEKS
        ChronoUnit.MONTHS -> com.infinum.sentinel.utils.ChronoUnit.MONTHS
        ChronoUnit.YEARS -> com.infinum.sentinel.utils.ChronoUnit.YEARS
        ChronoUnit.DECADES -> com.infinum.sentinel.utils.ChronoUnit.DECADES
        ChronoUnit.CENTURIES -> com.infinum.sentinel.utils.ChronoUnit.CENTURIES
        ChronoUnit.MILLENNIA -> com.infinum.sentinel.utils.ChronoUnit.MILLENNIA
        ChronoUnit.ERAS -> com.infinum.sentinel.utils.ChronoUnit.ERAS
        ChronoUnit.FOREVER -> com.infinum.sentinel.utils.ChronoUnit.FOREVER
    }
