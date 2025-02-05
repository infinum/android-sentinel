package com.infinum.sentinel.ui.main.preferences.targeted

internal sealed class TargetedPreferencesEvent {

    class Cached : TargetedPreferencesEvent()
}
