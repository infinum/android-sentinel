package com.infinum.sentinel.ui.bundles.details

import android.os.Bundle
import androidx.annotation.RestrictTo
import com.infinum.sentinel.ui.Presentation
import com.infinum.sentinel.ui.shared.base.BaseActivity

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class BundleDetailsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager.beginTransaction()
            .replace(
                android.R.id.content,
                BundleDetailsFragment.newInstance(intent.extras?.getString(Presentation.Constants.KEY_BUNDLE_ID)),
                BundleDetailsFragment.TAG
            )
            .commit()
    }
}
