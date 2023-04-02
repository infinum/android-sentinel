package com.infinum.sentinel.ui.crash.details

import android.os.Bundle
import androidx.annotation.RestrictTo
import com.infinum.sentinel.ui.shared.Constants
import com.infinum.sentinel.ui.shared.base.BaseChildActivity

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class CrashDetailsActivity : BaseChildActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent?.getLongExtra(Constants.Keys.CRASH_ID, -1L)
            ?.takeUnless { it == -1L }
            ?.let {
                supportFragmentManager.beginTransaction()
                    .replace(
                        android.R.id.content,
                        CrashDetailsFragment.newInstance(it),
                        CrashDetailsFragment.TAG
                    )
                    .commit()
            } ?: finish()
    }
}
