package com.infinum.sentinel.ui.main

internal sealed class SentinelEvent {
    data class Formatted(
        val value: String,
    ) : SentinelEvent()
}
