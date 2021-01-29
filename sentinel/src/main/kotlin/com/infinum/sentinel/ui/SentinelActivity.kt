package com.infinum.sentinel.ui

import android.os.Bundle
import androidx.annotation.RestrictTo
import com.infinum.sentinel.ui.shared.base.BaseActivity

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class SentinelActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SentinelFragment().show(supportFragmentManager, SentinelFragment.TAG)
    }
}
