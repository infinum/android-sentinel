package com.infinum.sentinel.ui.main.preferences.editor

import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.RestrictTo
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.infinum.sentinel.R
import com.infinum.sentinel.data.models.raw.PreferenceType
import com.infinum.sentinel.databinding.SentinelFragmentPreferenceEditorBinding
import com.infinum.sentinel.databinding.SentinelViewItemInputBinding
import com.infinum.sentinel.ui.Presentation
import com.infinum.sentinel.ui.shared.base.BaseChildFragment
import com.infinum.sentinel.ui.shared.delegates.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class PreferenceEditorFragment :
    BaseChildFragment<Any, PreferenceEditorEvent>(R.layout.sentinel_fragment_preference_editor) {

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
    private var type: PreferenceType = PreferenceType.UNKNOWN
    private var key: String? = null
    private var value: Any? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fileName = arguments?.getString(Presentation.Constants.KEY_PREFERENCE_FILE)
        type = PreferenceType.values()
            .firstOrNull { it.ordinal == arguments?.getInt(Presentation.Constants.KEY_PREFERENCE_TYPE) }
            ?: PreferenceType.UNKNOWN
        key = arguments?.getString(Presentation.Constants.KEY_PREFERENCE_KEY)
        value = when (type) {
            PreferenceType.BOOLEAN -> arguments?.getBoolean(Presentation.Constants.KEY_PREFERENCE_VALUE)
            PreferenceType.FLOAT -> arguments?.getFloat(Presentation.Constants.KEY_PREFERENCE_VALUE)
            PreferenceType.INT -> arguments?.getInt(Presentation.Constants.KEY_PREFERENCE_VALUE)
            PreferenceType.LONG -> arguments?.getLong(Presentation.Constants.KEY_PREFERENCE_VALUE)
            PreferenceType.STRING -> arguments?.getString(Presentation.Constants.KEY_PREFERENCE_VALUE)
            PreferenceType.SET -> arguments?.getStringArray(Presentation.Constants.KEY_PREFERENCE_VALUE)
            else -> throw IllegalArgumentException("Unknown preference type.")
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            toolbar.setNavigationOnClickListener { requireActivity().finish() }
            toolbar.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.save -> {
                        when (type) {
                            PreferenceType.BOOLEAN -> viewModel.saveBoolean(
                                fileName,
                                key,
                                value as? Boolean,
                                when {
                                    trueButton.isChecked -> true
                                    falseButton.isChecked -> false
                                    else -> null
                                }
                            )
                            PreferenceType.FLOAT -> viewModel.saveFloat(
                                fileName,
                                key,
                                value as? Float,
                                newValueInputLayout.editText?.text?.toString().orEmpty().trim().toFloatOrNull()
                            )
                            PreferenceType.INT -> viewModel.saveInteger(
                                fileName,
                                key,
                                value as? Int,
                                newValueInputLayout.editText?.text?.toString().orEmpty().trim().toIntOrNull()
                            )
                            PreferenceType.LONG -> viewModel.saveLong(
                                fileName,
                                key,
                                value as? Long,
                                newValueInputLayout.editText?.text?.toString().orEmpty().trim().toLongOrNull()
                            )
                            PreferenceType.STRING -> viewModel.saveString(
                                fileName,
                                key,
                                value as? String,
                                newValueInputLayout.editText?.text?.toString()?.trim()
                            )
                            PreferenceType.SET -> viewModel.saveArray(
                                fileName,
                                key,
                                value as? Array<String>,
                                setLayout
                                    .children
                                    .filterIsInstance<FrameLayout>()
                                    .map { frameLayout -> frameLayout.getChildAt(0) }
                                    .filterIsInstance<TextInputLayout>()
                                    .map { textInputLayout ->
                                        textInputLayout
                                            .editText
                                            ?.text
                                            ?.toString()
                                            .orEmpty()
                                            .trim()
                                    }
                                    .toList()
                                    .toTypedArray()
                            )
                            else -> Unit
                        }
                        true
                    }
                    else -> false
                }
            }
            preferencesView.text = fileName
            keyView.text = key
            currentValueView.text = when (type) {
                PreferenceType.BOOLEAN -> (value as? Boolean)?.toString()
                PreferenceType.FLOAT -> (value as? Float)?.toString()
                PreferenceType.INT -> (value as? Int)?.toString()
                PreferenceType.LONG -> (value as? Long)?.toString()
                PreferenceType.STRING -> value as? String
                PreferenceType.SET -> (value as? Array<*>)?.contentToString()
                else -> null
            }
            when (type) {
                PreferenceType.BOOLEAN -> {
                    toggleGroup.isVisible = true
                    trueButton.isChecked = (value as? Boolean)?.equals(true) ?: false
                    falseButton.isChecked = (value as? Boolean)?.equals(false) ?: false
                    newValueInputLayout.isVisible = false
                    setLayout.isVisible = false
                }
                PreferenceType.FLOAT -> {
                    toggleGroup.isVisible = false
                    newValueInputLayout.isVisible = true
                    setLayout.isVisible = false
                    newValueInputLayout.editText?.setText((value as? Float)?.toString())
                }
                PreferenceType.INT -> {
                    toggleGroup.isVisible = false
                    newValueInputLayout.isVisible = true
                    setLayout.isVisible = false
                    newValueInputLayout.editText?.setText((value as? Int)?.toString())
                }
                PreferenceType.LONG -> {
                    toggleGroup.isVisible = false
                    newValueInputLayout.isVisible = true
                    setLayout.isVisible = false
                    newValueInputLayout.editText?.setText((value as? Long)?.toString())
                }
                PreferenceType.STRING -> {
                    toggleGroup.isVisible = false
                    newValueInputLayout.isVisible = true
                    setLayout.isVisible = false
                    newValueInputLayout.editText?.setText(value as? String)
                }
                PreferenceType.SET -> {
                    toggleGroup.isVisible = false
                    newValueInputLayout.isVisible = false
                    setLayout.isVisible = true
                    addButton.setOnClickListener {
                        setLayout.addView(
                            SentinelViewItemInputBinding.inflate(layoutInflater, setLayout, false)
                                .apply {
                                    inputLayout.editText?.setText("")
                                    inputLayout.setStartIconOnClickListener {
                                        setLayout.removeView(this.root)
                                    }
                                    inputLayout.setEndIconOnClickListener {
                                        inputLayout.editText?.setText("")
                                    }
                                }.root
                        )
                    }
                    (value as? Array<*>)
                        ?.mapNotNull { (it as? String) }
                        .orEmpty()
                        .forEach { value ->
                            setLayout.addView(
                                SentinelViewItemInputBinding.inflate(layoutInflater, setLayout, false)
                                    .apply {
                                        inputLayout.editText?.setText(value)
                                        inputLayout.setStartIconOnClickListener {
                                            setLayout.removeView(this.root)
                                        }
                                        inputLayout.setEndIconOnClickListener {
                                            inputLayout.editText?.setText("")
                                        }
                                    }.root
                            )
                        }
                }
                else -> {
                    toggleGroup.isVisible = false
                    newValueInputLayout.isVisible = false
                    setLayout.isVisible = false
                }
            }
            newValueInput.inputType = when (type) {
                PreferenceType.BOOLEAN -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
                PreferenceType.FLOAT -> InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
                PreferenceType.INT -> InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL
                PreferenceType.LONG -> InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_NORMAL
                PreferenceType.STRING -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
                PreferenceType.SET -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
                else -> InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
            }
            newValueInput.doOnTextChanged { text, _, _, _ ->
                val newValue = text?.toString().orEmpty().trim()
                when (type) {
                    PreferenceType.BOOLEAN -> {
                        newValueInputLayout.error = null
                    }
                    PreferenceType.FLOAT -> {
                        newValue.toFloatOrNull()?.let {
                            newValueInputLayout.error = null
                        } ?: run {
                            newValueInputLayout.error = "Invalid float number."
                        }
                    }
                    PreferenceType.INT -> {
                        newValue.toIntOrNull()?.let {
                            newValueInputLayout.error = null
                        } ?: run {
                            newValueInputLayout.error = "Invalid integer number."
                        }
                    }
                    PreferenceType.LONG -> {
                        newValue.toLongOrNull()?.let {
                            newValueInputLayout.error = null
                        } ?: run {
                            newValueInputLayout.error = "Invalid long number."
                        }
                    }
                    PreferenceType.STRING -> {
                        newValueInputLayout.error = null
                    }
                    PreferenceType.SET -> {
                        newValueInputLayout.error = null
                    }
                    else -> {
                        newValueInputLayout.error = "Unknown preference type."
                    }
                }
            }
        }
    }

    override fun onState(state: Any) = Unit

    override fun onEvent(event: PreferenceEditorEvent) =
        when (event) {
            is PreferenceEditorEvent.Saved -> {
                Snackbar.make(binding.root, "New value saved.", Snackbar.LENGTH_SHORT)
                    .addCallback(object : Snackbar.Callback() {
                        override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                            activity?.finish()
                        }
                    }).show()
            }
        }
}
