package com.infinum.sentinel.ui.crash.details

internal sealed class CrashDetailsEvent {

    class Removed : CrashDetailsEvent()

    data class Formatted(
        val value: String
    ) : CrashDetailsEvent()
}
