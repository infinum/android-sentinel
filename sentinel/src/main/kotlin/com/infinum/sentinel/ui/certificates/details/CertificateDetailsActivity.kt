package com.infinum.sentinel.ui.certificates.details

import android.os.Bundle
import androidx.annotation.RestrictTo
import com.infinum.sentinel.ui.shared.base.BaseChildActivity

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class CertificateDetailsActivity : BaseChildActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager.beginTransaction()
            .replace(
                android.R.id.content,
                CertificateDetailsFragment.newInstance(),
                CertificateDetailsFragment.TAG
            )
            .commit()
    }
}
