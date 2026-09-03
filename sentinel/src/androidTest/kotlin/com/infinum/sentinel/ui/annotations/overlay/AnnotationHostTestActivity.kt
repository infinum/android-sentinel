package com.infinum.sentinel.ui.annotations.overlay

import android.os.Bundle
import android.widget.FrameLayout
import androidx.fragment.app.FragmentActivity

/**
 * Stand-in host Activity for instrumented tests in this package.
 *
 * `AnnotationOverlayController` attaches its overlay to `window.decorView` of whichever
 * Activity is resumed, and `fragmentNameOf` walks `supportFragmentManager` — so the test host
 * must be a real, manifest-declared [FragmentActivity], not a bare `android.app.Activity` (the
 * androidTest manifest declares no such component, so `ActivityScenario.launch(Activity::class
 * .java)` cannot launch one).
 */
internal class AnnotationHostTestActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(FrameLayout(this))
    }
}
