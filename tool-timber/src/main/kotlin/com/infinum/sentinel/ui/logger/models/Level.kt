package com.infinum.sentinel.ui.logger.models

import android.util.Log

internal enum class Level {
    UNKNOWN,
    ASSERT,
    DEBUG,
    ERROR,
    INFO,
    VERBOSE,
    WARN;

    companion object {
        fun forLogLevel(logLevel: Int): Level =
            when (logLevel) {
                Log.ASSERT -> ASSERT
                Log.DEBUG -> DEBUG
                Log.ERROR -> ERROR
                Log.INFO -> INFO
                Log.VERBOSE -> VERBOSE
                Log.WARN -> WARN
                else -> UNKNOWN
            }
    }
}
