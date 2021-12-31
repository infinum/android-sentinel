package com.infinum.sentinel.ui.main.preferences.editor

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.infinum.sentinel.ui.Presentation

internal class PreferenceEditorContract : ActivityResultContract<Unit, Boolean>() {

    override fun createIntent(context: Context, input: Unit): Intent =
        Intent(context, PreferenceEditorActivity::class.java)

    override fun parseResult(resultCode: Int, intent: Intent?): Boolean =
        if (resultCode == Activity.RESULT_OK) {
            intent?.getBooleanExtra(
                Presentation.Constants.Keys.SHOULD_REFRESH,
                false
            ) ?: false
        } else {
            false
        }
}
