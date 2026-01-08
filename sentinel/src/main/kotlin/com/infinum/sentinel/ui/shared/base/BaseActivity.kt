package com.infinum.sentinel.ui.shared.base

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.CallSuper
import androidx.annotation.RestrictTo
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentActivity
import com.infinum.sentinel.ui.main.SentinelActivity

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal abstract class BaseActivity<State, Event> : FragmentActivity(), BaseView<State, Event> {

    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> false
            Configuration.UI_MODE_NIGHT_NO -> true
            else -> null
        }?.let {
            WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = it
        } ?: run { WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true }

        collectFlows(this)
    }

    @CallSuper
    override fun onPause() =
        super.onPause().run {
            overridePendingTransition(0, 0)
        }
}
