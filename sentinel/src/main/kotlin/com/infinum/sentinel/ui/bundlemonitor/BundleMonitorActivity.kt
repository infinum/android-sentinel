package com.infinum.sentinel.ui.bundlemonitor

import android.os.Bundle
import androidx.annotation.RestrictTo
import com.infinum.sentinel.ui.shared.base.BaseActivity

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class BundleMonitorActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, BundleMonitorFragment.newInstance(), BundleMonitorFragment.TAG)
            .commit()
    }
}
