package com.infinum.sentinel.ui.networkproxy

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

// The rule form and the body editor share this single copy of the body. Passing it through fragment arguments and
// each fragment's saved state would store it several times in the activity's saved state, which can the app
internal class NetworkProxyBodyViewModel(
    private val savedState: SavedStateHandle,
) : ViewModel() {
    var body: String
        get() = savedState.get<String>(KEY_BODY).orEmpty()
        set(value) {
            savedState[KEY_BODY] = value
        }

    companion object {
        private const val KEY_BODY = "body"
    }
}
