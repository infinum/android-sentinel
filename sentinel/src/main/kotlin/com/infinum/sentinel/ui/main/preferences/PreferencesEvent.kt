package com.infinum.sentinel.ui.main.preferences

internal sealed class PreferencesEvent {
    class Cached : PreferencesEvent()
}
