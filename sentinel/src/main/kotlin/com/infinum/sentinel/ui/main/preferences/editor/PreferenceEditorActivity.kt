package com.infinum.sentinel.ui.main.preferences.editor

import android.os.Bundle
import androidx.annotation.RestrictTo
import com.infinum.sentinel.data.models.raw.PreferenceType
import com.infinum.sentinel.ui.Presentation
import com.infinum.sentinel.ui.shared.base.BaseChildActivity

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class PreferenceEditorActivity : BaseChildActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val typeOrdinal = intent.extras?.getInt(
            Presentation.Constants.KEY_PREFERENCE_TYPE,
            PreferenceType.UNKNOWN.ordinal
        )

        supportFragmentManager.beginTransaction()
            .replace(
                android.R.id.content,
                PreferenceEditorFragment.newInstance(
                    intent.extras?.getString(Presentation.Constants.KEY_PREFERENCE_FILE),
                    typeOrdinal,
                    intent.extras?.getString(Presentation.Constants.KEY_PREFERENCE_KEY),
                    when (PreferenceType.values().firstOrNull { it.ordinal == typeOrdinal }) {
                        PreferenceType.BOOLEAN -> intent.extras?.getBoolean(Presentation.Constants.KEY_PREFERENCE_VALUE)
                        PreferenceType.FLOAT -> intent.extras?.getFloat(Presentation.Constants.KEY_PREFERENCE_VALUE)
                        PreferenceType.INT -> intent.extras?.getInt(Presentation.Constants.KEY_PREFERENCE_VALUE)
                        PreferenceType.LONG -> intent.extras?.getLong(Presentation.Constants.KEY_PREFERENCE_VALUE)
                        PreferenceType.STRING -> intent.extras?.getString(Presentation.Constants.KEY_PREFERENCE_VALUE)
                        PreferenceType.SET -> intent.extras?.getStringArray(Presentation.Constants.KEY_PREFERENCE_VALUE)
                        else -> throw IllegalArgumentException("Unknown preference type.")
                    }
                ),
                PreferenceEditorFragment.TAG
            )
            .commit()
    }
}
