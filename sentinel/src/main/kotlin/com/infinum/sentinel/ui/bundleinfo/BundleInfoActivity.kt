package com.infinum.sentinel.ui.bundleinfo

import android.os.Bundle
import androidx.annotation.RestrictTo
import com.infinum.sentinel.ui.shared.base.BaseActivity

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class BundleInfoActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, BundleInfoFragment.newInstance(), BundleInfoFragment.TAG)
            .commit()
    }
}