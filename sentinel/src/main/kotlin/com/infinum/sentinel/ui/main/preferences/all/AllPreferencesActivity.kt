package com.infinum.sentinel.ui.main.preferences.all

import android.os.Bundle
import androidx.annotation.RestrictTo
import androidx.core.view.WindowCompat
import com.infinum.sentinel.ui.main.preferences.PreferencesFragment
import com.infinum.sentinel.ui.shared.base.BaseChildActivity

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class AllPreferencesActivity : BaseChildActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)

        supportFragmentManager.beginTransaction()
            .replace(
                android.R.id.content,
                PreferencesFragment.newInstance(PreferencesFragment.ALL_PREFERENCES),
                PreferencesFragment.TAG
            )
            .commit()
    }
}
