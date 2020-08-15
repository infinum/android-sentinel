package com.infinum.sentinel.ui.settings

import android.os.Bundle
import androidx.annotation.RestrictTo
import com.infinum.sentinel.ui.shared.BaseActivity

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content, SettingsFragment.newInstance(), SettingsFragment.TAG)
            .commit()
    }
}
