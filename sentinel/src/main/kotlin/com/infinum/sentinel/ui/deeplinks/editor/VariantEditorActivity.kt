package com.infinum.sentinel.ui.deeplinks.editor

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.annotation.RestrictTo
import com.infinum.sentinel.ui.shared.base.BaseChildActivity

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class VariantEditorActivity : BaseChildActivity() {
    companion object {
        const val KEY_VARIANT_ID: String = "KEY_VARIANT_EDITOR_VARIANT_ID"
        const val KEY_BASE_KEY: String = "KEY_VARIANT_EDITOR_BASE_KEY"
        const val KEY_COMPONENT_NAME: String = "KEY_VARIANT_EDITOR_COMPONENT_NAME"
        const val KEY_FOCUS: String = "KEY_VARIANT_EDITOR_FOCUS"

        /**
         * Creates the intent that opens the editor for [target]. The editor focuses the field named by [focus].
         */
        fun newIntent(
            context: Context,
            target: VariantEditorTarget,
            focus: VariantEditorFocus = VariantEditorFocus.NONE,
        ): Intent =
            Intent(context, VariantEditorActivity::class.java).apply {
                when (target) {
                    is VariantEditorTarget.NewFromBase -> {
                        putExtra(KEY_BASE_KEY, target.baseKey)
                        target.componentName?.let { putExtra(KEY_COMPONENT_NAME, it) }
                    }
                    is VariantEditorTarget.Existing -> putExtra(KEY_VARIANT_ID, target.variantId)
                }
                putExtra(KEY_FOCUS, focus.name)
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(
                    android.R.id.content,
                    VariantEditorFragment.newInstance(intent.extras ?: Bundle()),
                    VariantEditorFragment.TAG,
                ).commit()
        }
    }
}
