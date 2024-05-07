package com.infinum.sentinel.utils

import android.os.Build
import androidx.annotation.RequiresApi

/**
 * Sentinel's implementation of Java 8 Time API ChronoUnit to avoid dependency on Java 8 in Database
 * @see [https://docs.oracle.com/javase/8/docs/api/java/time/temporal/ChronoUnit.html]
 */
internal enum class ChronoUnit {
    NANOS,
    MICROS,
    MILLIS,
    SECONDS,
    MINUTES,
    HOURS,
    HALF_DAYS,
    DAYS,
    WEEKS,
    MONTHS,
    YEARS,
    DECADES,
    CENTURIES,
    MILLENNIA,
    ERAS,
    FOREVER
}

@RequiresApi(Build.VERSION_CODES.O)
internal fun ChronoUnit.toJavaChronoUnit(): java.time.temporal.ChronoUnit =
    when (this) {
        ChronoUnit.NANOS -> java.time.temporal.ChronoUnit.NANOS
        ChronoUnit.MICROS -> java.time.temporal.ChronoUnit.MICROS
        ChronoUnit.MILLIS -> java.time.temporal.ChronoUnit.MILLIS
        ChronoUnit.SECONDS -> java.time.temporal.ChronoUnit.SECONDS
        ChronoUnit.MINUTES -> java.time.temporal.ChronoUnit.MINUTES
        ChronoUnit.HOURS -> java.time.temporal.ChronoUnit.HOURS
        ChronoUnit.HALF_DAYS -> java.time.temporal.ChronoUnit.HALF_DAYS
        ChronoUnit.DAYS -> java.time.temporal.ChronoUnit.DAYS
        ChronoUnit.WEEKS -> java.time.temporal.ChronoUnit.WEEKS
        ChronoUnit.MONTHS -> java.time.temporal.ChronoUnit.MONTHS
        ChronoUnit.YEARS -> java.time.temporal.ChronoUnit.YEARS
        ChronoUnit.DECADES -> java.time.temporal.ChronoUnit.DECADES
        ChronoUnit.CENTURIES -> java.time.temporal.ChronoUnit.CENTURIES
        ChronoUnit.MILLENNIA -> java.time.temporal.ChronoUnit.MILLENNIA
        ChronoUnit.ERAS -> java.time.temporal.ChronoUnit.ERAS
        ChronoUnit.FOREVER -> java.time.temporal.ChronoUnit.FOREVER
    }
