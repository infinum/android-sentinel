package com.infinum.sentinel.ui.shared.base

import androidx.annotation.RestrictTo
import androidx.fragment.app.FragmentActivity

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal abstract class BaseActivity : FragmentActivity() {

    override fun onPause() =
        super.onPause().run {
            overridePendingTransition(0, 0)
        }
}
