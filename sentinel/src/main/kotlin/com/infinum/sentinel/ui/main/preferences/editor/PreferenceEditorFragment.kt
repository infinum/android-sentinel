package com.infinum.sentinel.ui.main.preferences.editor

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import androidx.annotation.RestrictTo
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.core.widget.doOnTextChanged
import com.google.android.material.textfield.TextInputLayout
import com.infinum.sentinel.R
import com.infinum.sentinel.data.models.raw.PreferenceType
import com.infinum.sentinel.databinding.SentinelFragmentPreferenceEditorBinding
import com.infinum.sentinel.databinding.SentinelViewItemInputBinding
import com.infinum.sentinel.extensions.viewModels
import com.infinum.sentinel.ui.shared.Constants
import com.infinum.sentinel.ui.shared.base.BaseChildFragment
import com.infinum.sentinel.ui.shared.delegates.viewBinding

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class PreferenceEditorFragment :
    BaseChildFragment<PreferenceEditorState, PreferenceEditorEvent>(R.layout.sentinel_fragment_preference_editor) {

    companion object {
        fun newInstance() = PreferenceEditorFragment()

        const val TAG: String = "PreferenceEditorFragment"
    }

    override val binding: SentinelFragmentPreferenceEditorBinding by viewBinding(
        SentinelFragmentPreferenceEditorBinding::bind
    )

    override val viewModel: PreferenceEditorViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            toolbar.setNavigationOnClickListener {
                with(requireActivity()) {
                    setResult(Activity.RESULT_CANCELED)
                    finish()
                }
            }
        }
    }

    @Suppress("UNCHECKED_CAST", "LongMethod", "ComplexMethod", "NestedBlockDepth")
    override fun onState(state: PreferenceEditorState) {
        when (state) {
            is PreferenceEditorState.Cache -> {
                with(binding) {
                    toolbar.setOnMenuItemClickListener {
                        when (it.itemId) {
                            R.id.save -> {
                                closeKeyboard()
                                binding.progressBar.isVisible = true
                                when (state.type) {
                                    PreferenceType.BOOLEAN -> viewModel.saveBoolean(
                                        state.name,
                                        state.key,
                                        state.value as? Boolean,
                                        when {
                                            trueButton.isChecked -> true
                                            falseButton.isChecked -> false
                                            else -> null
                                        }
                                    )

                                    PreferenceType.FLOAT -> viewModel.saveFloat(
                                        state.name,
                                        state.key,
                                        state.value as? Float,
                                        newValueInputLayout.editText?.text?.toString().orEmpty().trim().toFloatOrNull()
                                    )

                                    PreferenceType.INT -> viewModel.saveInteger(
                                        state.name,
                                        state.key,
                                        state.value as? Int,
                                        newValueInputLayout.editText?.text?.toString().orEmpty().trim().toIntOrNull()
                                    )

                                    PreferenceType.LONG -> viewModel.saveLong(
                                        state.name,
                                        state.key,
                                        state.value as? Long,
                                        newValueInputLayout.editText?.text?.toString().orEmpty().trim().toLongOrNull()
                                    )

                                    PreferenceType.STRING -> viewModel.saveString(
                                        state.name,
                                        state.key,
                                        state.value as? String,
                                        newValueInputLayout.editText?.text?.toString()?.trim()
                                    )

                                    PreferenceType.SET -> viewModel.saveArray(
                                        state.name,
                                        state.key,
                                        (state.value as? HashSet<String>)?.toTypedArray(),
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
                    preferencesView.text = state.name
                    keyView.text = state.key
                    currentValueView.text = when (state.type) {
                        PreferenceType.BOOLEAN -> (state.value as? Boolean)?.toString()
                        PreferenceType.FLOAT -> (state.value as? Float)?.toString()
                        PreferenceType.INT -> (state.value as? Int)?.toString()
                        PreferenceType.LONG -> (state.value as? Long)?.toString()
                        PreferenceType.STRING -> state.value as? String
                        PreferenceType.SET -> (state.value as? Array<*>)?.contentToString()
                        else -> null
                    }
                    when (state.type) {
                        PreferenceType.BOOLEAN -> {
                            toggleGroup.isVisible = true
                            trueButton.isChecked = (state.value as? Boolean)?.equals(true) ?: false
                            falseButton.isChecked = (state.value as? Boolean)?.equals(false) ?: false
                            newValueInputLayout.isVisible = false
                            setLayout.isVisible = false
                        }

                        PreferenceType.FLOAT -> {
                            toggleGroup.isVisible = false
                            newValueInputLayout.isVisible = true
                            setLayout.isVisible = false
                            newValueInputLayout.editText?.setText((state.value as? Float)?.toString())
                        }

                        PreferenceType.INT -> {
                            toggleGroup.isVisible = false
                            newValueInputLayout.isVisible = true
                            setLayout.isVisible = false
                            newValueInputLayout.editText?.setText((state.value as? Int)?.toString())
                        }

                        PreferenceType.LONG -> {
                            toggleGroup.isVisible = false
                            newValueInputLayout.isVisible = true
                            setLayout.isVisible = false
                            newValueInputLayout.editText?.setText((state.value as? Long)?.toString())
                        }

                        PreferenceType.STRING -> {
                            toggleGroup.isVisible = false
                            newValueInputLayout.isVisible = true
                            setLayout.isVisible = false
                            newValueInputLayout.editText?.setText(state.value as? String)
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
                            (state.value as? HashSet<*>)
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
                    newValueInput.inputType = when (state.type) {
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
                        when (state.type) {
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
        }
    }

    override fun onEvent(event: PreferenceEditorEvent) {
        when (event) {
            is PreferenceEditorEvent.Saved -> {
                binding.progressBar.isVisible = false
                activity?.let {
                    it.setResult(
                        Activity.RESULT_OK,
                        Intent().apply {
                            putExtra(Constants.Keys.SHOULD_REFRESH, true)
                        }
                    )
                    it.finish()
                }
            }
        }
    }

    override fun onError(error: Throwable) {
        super.onError(error)
        binding.progressBar.isVisible = false
    }

    private fun closeKeyboard() {
        val imm = ContextCompat.getSystemService(requireContext(), InputMethodManager::class.java)
        imm?.hideSoftInputFromWindow(binding.newValueInputLayout.windowToken, 0)
    }
}
