package com.infinum.sentinel.ui.main.preferences.editor

import android.os.Bundle
import android.view.View
import androidx.annotation.RestrictTo
import com.infinum.sentinel.R
import com.infinum.sentinel.data.models.raw.PreferenceType
import com.infinum.sentinel.databinding.SentinelFragmentPreferenceEditorBinding
import com.infinum.sentinel.ui.Presentation
import com.infinum.sentinel.ui.shared.base.BaseChildFragment
import com.infinum.sentinel.ui.shared.delegates.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class PreferenceEditorFragment : BaseChildFragment<Any, Any>(R.layout.sentinel_fragment_preference_editor) {

    @Suppress("UNCHECKED_CAST")
    companion object {
        fun newInstance(
            fileName: String?,
            type: Int?,
            key: String?,
            value: Any?
        ) = PreferenceEditorFragment().apply {
            arguments = Bundle().apply {
                putString(Presentation.Constants.KEY_PREFERENCE_FILE, fileName)
                type?.let { putInt(Presentation.Constants.KEY_PREFERENCE_TYPE, it) }
                putString(Presentation.Constants.KEY_PREFERENCE_KEY, key)
                when (PreferenceType.values().firstOrNull { it.ordinal == type }) {
                    PreferenceType.BOOLEAN -> putBoolean(Presentation.Constants.KEY_PREFERENCE_VALUE, value as Boolean)
                    PreferenceType.FLOAT -> putFloat(Presentation.Constants.KEY_PREFERENCE_VALUE, value as Float)
                    PreferenceType.INT -> putInt(Presentation.Constants.KEY_PREFERENCE_VALUE, value as Int)
                    PreferenceType.LONG -> putLong(Presentation.Constants.KEY_PREFERENCE_VALUE, value as Long)
                    PreferenceType.STRING -> putString(Presentation.Constants.KEY_PREFERENCE_VALUE, value as String)
                    PreferenceType.SET -> putStringArray(
                        Presentation.Constants.KEY_PREFERENCE_VALUE,
                        value as Array<out String>
                    )
                    else -> throw IllegalArgumentException("Unknown preference type.")
                }
            }
        }

        const val TAG: String = "PreferenceEditorFragment"
    }

    override val binding: SentinelFragmentPreferenceEditorBinding by viewBinding(
        SentinelFragmentPreferenceEditorBinding::bind
    )

    override val viewModel: PreferenceEditorViewModel by viewModel()

    private var fileName: String? = null
    private var typeOrdinal: Int? = null
    private var key: String? = null
    private var value: Any? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fileName = arguments?.getString(Presentation.Constants.KEY_PREFERENCE_FILE)
        typeOrdinal = arguments?.getInt(Presentation.Constants.KEY_PREFERENCE_TYPE)
        key = arguments?.getString(Presentation.Constants.KEY_PREFERENCE_KEY)
        value = when (PreferenceType.values().firstOrNull { it.ordinal == typeOrdinal }) {
            PreferenceType.BOOLEAN -> arguments?.getBoolean(Presentation.Constants.KEY_PREFERENCE_VALUE)
            PreferenceType.FLOAT -> arguments?.getFloat(Presentation.Constants.KEY_PREFERENCE_VALUE)
            PreferenceType.INT -> arguments?.getInt(Presentation.Constants.KEY_PREFERENCE_VALUE)
            PreferenceType.LONG -> arguments?.getLong(Presentation.Constants.KEY_PREFERENCE_VALUE)
            PreferenceType.STRING -> arguments?.getString(Presentation.Constants.KEY_PREFERENCE_VALUE)
            PreferenceType.SET -> arguments?.getStringArray(Presentation.Constants.KEY_PREFERENCE_VALUE)
            else -> throw IllegalArgumentException("Unknown preference type.")
        }
        viewModel.data(fileName, key, PreferenceType.values().firstOrNull { it.ordinal == typeOrdinal }, value)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
    }

    override fun onState(state: Any) = Unit

    override fun onEvent(event: Any) = Unit

    private fun setupToolbar() {
        with(binding) {
            toolbar.setNavigationOnClickListener { requireActivity().finish() }
        }
    }
}
