package com.infinum.sentinel.ui.crash

import android.os.Bundle
import androidx.annotation.RestrictTo
import com.infinum.sentinel.ui.Presentation
import com.infinum.sentinel.ui.shared.base.BaseChildActivity

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class CrashesActivity : BaseChildActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager.beginTransaction()
            .replace(
                android.R.id.content,
                CrashesFragment.newInstance(intent.getStringExtra(Presentation.Constants.Keys.APPLICATION_NAME)),
                CrashesFragment.TAG
            )
            .commit()
    }
}
