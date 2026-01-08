package com.infinum.sentinel.ui.main.preferences.editor

import android.os.Bundle
import androidx.annotation.RestrictTo
import com.infinum.sentinel.ui.shared.base.BaseChildActivity

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class PreferenceEditorActivity : BaseChildActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager
            .beginTransaction()
            .replace(
                android.R.id.content,
                PreferenceEditorFragment.newInstance(),
                PreferenceEditorFragment.TAG,
            ).commit()
    }
}
