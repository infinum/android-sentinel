package com.infinum.sentinel.ui.bundles

import android.os.Bundle
import androidx.annotation.RestrictTo
import com.infinum.sentinel.ui.shared.base.BaseActivity

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class BundlesActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, BundlesFragment.newInstance(), BundlesFragment.TAG)
            .commit()
    }
}
