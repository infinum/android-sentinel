package com.infinum.sentinel.ui.networkproxy

import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment

// The window stays full height behind the keyboard and the navigation bar in an edge-to-edge activity, so each
// screen reserves the larger of the two itself. The insets are returned unconsumed so the app bar inside the
// hierarchy still receives the status bar inset
internal fun View.doOnBottomInset(block: (bottomInset: Int) -> Unit) {
    ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
        val keyboard = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
        val navigationBar = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
        block(maxOf(keyboard, navigationBar))
        insets
    }
}

// A replaced screen takes its focused field with it, but the keyboard can stay up and type into nothing. System back
// closes the keyboard before it pops, any other way of leaving a screen with a text field has to call this first
internal fun Fragment.hideKeyboard() {
    WindowCompat.getInsetsController(requireActivity().window, requireView()).hide(WindowInsetsCompat.Type.ime())
}
