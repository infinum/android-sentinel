package com.infinum.sentinel.ui.certificates

import android.os.Build
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.annotation.RestrictTo
import com.infinum.sentinel.ui.shared.base.BaseChildActivity

@RestrictTo(RestrictTo.Scope.LIBRARY)
@RequiresApi(Build.VERSION_CODES.O)
internal class CertificatesActivity : BaseChildActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager
            .beginTransaction()
            .replace(android.R.id.content, CertificatesFragment.newInstance(), CertificatesFragment.TAG)
            .commit()
    }
}
