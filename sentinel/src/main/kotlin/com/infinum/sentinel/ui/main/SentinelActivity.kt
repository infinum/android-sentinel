package com.infinum.sentinel.ui.main

import android.os.Bundle
import androidx.annotation.RestrictTo
import com.infinum.sentinel.ui.shared.base.BaseActivity
import com.infinum.sentinel.ui.shared.base.BaseViewModel

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class SentinelActivity : BaseActivity<Nothing, Nothing>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SentinelFragment().show(supportFragmentManager, SentinelFragment.TAG)
    }

    override val viewModel: BaseViewModel<Nothing, Nothing>? = null

    override fun onState(state: Nothing) = Unit

    override fun onEvent(event: Nothing) = Unit
}
