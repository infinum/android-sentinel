package com.infinum.sentinel.ui.bundles.details

import android.os.Bundle
import androidx.annotation.RestrictTo
import com.infinum.sentinel.ui.shared.Constants
import com.infinum.sentinel.ui.shared.base.BaseChildActivity

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class BundleDetailsActivity : BaseChildActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager.beginTransaction()
            .replace(
                android.R.id.content,
                BundleDetailsFragment.newInstance(intent.extras?.getString(Constants.Keys.BUNDLE_ID)),
                BundleDetailsFragment.TAG
            )
            .commit()
    }
}
