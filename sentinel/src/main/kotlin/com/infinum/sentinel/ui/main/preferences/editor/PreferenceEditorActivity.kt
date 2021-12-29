package com.infinum.sentinel.ui.main.preferences.editor

import android.os.Bundle
import androidx.annotation.RestrictTo
import com.infinum.sentinel.ui.Presentation
import com.infinum.sentinel.ui.shared.base.BaseChildActivity

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class PreferenceEditorActivity : BaseChildActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val clazz = intent.extras?.getSerializable(Presentation.Constants.KEY_PREFERENCE_CLASS) as? Class<out Any>?

        supportFragmentManager.beginTransaction()
            .replace(
                android.R.id.content,
                PreferenceEditorFragment.newInstance(
                    intent.extras?.getString(Presentation.Constants.KEY_PREFERENCE_FILE),
                    clazz,
                    intent.extras?.getString(Presentation.Constants.KEY_PREFERENCE_KEY),
                    when (clazz) {
                        Boolean::class.java -> intent.extras?.getBoolean(Presentation.Constants.KEY_PREFERENCE_VALUE)
                        Float::class.java -> intent.extras?.getFloat(Presentation.Constants.KEY_PREFERENCE_VALUE)
                        Int::class.java -> intent.extras?.getInt(Presentation.Constants.KEY_PREFERENCE_VALUE)
                        Long::class.java -> intent.extras?.getLong(Presentation.Constants.KEY_PREFERENCE_VALUE)
                        String::class.java -> intent.extras?.getString(Presentation.Constants.KEY_PREFERENCE_VALUE)
                        Set::class.java -> intent.extras?.getStringArray(Presentation.Constants.KEY_PREFERENCE_VALUE)
                        else -> throw IllegalArgumentException()
                    }
                ),
                PreferenceEditorFragment.TAG
            )
            .commit()
    }
}
