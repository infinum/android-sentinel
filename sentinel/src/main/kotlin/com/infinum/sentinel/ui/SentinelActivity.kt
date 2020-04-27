package com.infinum.sentinel.ui

import android.os.Bundle
import androidx.annotation.RestrictTo
import com.infinum.sentinel.di.SentinelActivityComponent
import com.infinum.sentinel.ui.shared.BaseActivity
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class SentinelActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        loadKoinModules(SentinelActivityComponent.modules())
        SentinelFragment().show(supportFragmentManager, SentinelFragment.TAG)
    }

    override fun onDestroy() {
        unloadKoinModules(SentinelActivityComponent.modules())
        super.onDestroy()
    }
}
