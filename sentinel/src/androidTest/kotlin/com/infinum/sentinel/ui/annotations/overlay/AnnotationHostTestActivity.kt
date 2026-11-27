package com.infinum.sentinel.ui.annotations.overlay

import android.os.Bundle
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity

/**
 * Stand-in host Activity for instrumented tests in this package.
 *
 * `AnnotationOverlayController` attaches to the resumed Activity's `decorView` and
 * `fragmentNameOf` walks `supportFragmentManager`, so the host is a manifest-declared
 * [FragmentActivity].
 */
internal class AnnotationHostTestActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(FrameLayout(this))
    }
}
