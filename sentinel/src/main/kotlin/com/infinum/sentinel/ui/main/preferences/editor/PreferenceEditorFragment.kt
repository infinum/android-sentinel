package com.infinum.sentinel.ui.main.preferences.editor

import android.os.Bundle
import android.view.View
import androidx.annotation.RestrictTo
import com.infinum.sentinel.R
import com.infinum.sentinel.databinding.SentinelFragmentPreferenceEditorBinding
import com.infinum.sentinel.ui.Presentation
import com.infinum.sentinel.ui.shared.base.BaseChildFragment
import com.infinum.sentinel.ui.shared.delegates.viewBinding
import java.io.Serializable
import org.koin.androidx.viewmodel.ext.android.viewModel

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class PreferenceEditorFragment : BaseChildFragment<Nothing, Nothing>(R.layout.sentinel_fragment_preference_editor) {

    @Suppress("UNCHECKED_CAST")
    companion object {
        fun newInstance(
            fileName: String?,
            clazz: Class<out Any>?,
            key: String?,
            value: Any?
        ) = PreferenceEditorFragment().apply {
            arguments = Bundle().apply {
                putString(Presentation.Constants.KEY_PREFERENCE_FILE, fileName)
                putSerializable(Presentation.Constants.KEY_PREFERENCE_CLASS, clazz)
                putString(Presentation.Constants.KEY_PREFERENCE_KEY, key)
                when (clazz) {
                    Boolean::class.java -> putBoolean(Presentation.Constants.KEY_PREFERENCE_VALUE, value as Boolean)
                    Float::class.java -> putFloat(Presentation.Constants.KEY_PREFERENCE_VALUE, value as Float)
                    Int::class.java -> putInt(Presentation.Constants.KEY_PREFERENCE_VALUE, value as Int)
                    Long::class.java -> putLong(Presentation.Constants.KEY_PREFERENCE_VALUE, value as Long)
                    String::class.java -> putString(Presentation.Constants.KEY_PREFERENCE_VALUE, value as String)
                    Set::class.java -> putStringArray(Presentation.Constants.KEY_PREFERENCE_VALUE, value as Array<out String>)
                    else -> throw IllegalArgumentException()
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
    private var clazz: Class<out Any>? = null
    private var key: String? = null
    private var value: Any? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fileName = arguments?.getString(Presentation.Constants.KEY_PREFERENCE_FILE)
        clazz = arguments?.getSerializable(Presentation.Constants.KEY_PREFERENCE_CLASS) as? Class<out Any>?
        key = arguments?.getString(Presentation.Constants.KEY_PREFERENCE_KEY)
        value = when (clazz) {
            Boolean::class.java -> arguments?.getBoolean(Presentation.Constants.KEY_PREFERENCE_VALUE)
            Float::class.java -> arguments?.getFloat(Presentation.Constants.KEY_PREFERENCE_VALUE)
            Int::class.java -> arguments?.getInt(Presentation.Constants.KEY_PREFERENCE_VALUE)
            Long::class.java -> arguments?.getLong(Presentation.Constants.KEY_PREFERENCE_VALUE)
            String::class.java -> arguments?.getString(Presentation.Constants.KEY_PREFERENCE_VALUE)
            Set::class.java -> arguments?.getStringArray(Presentation.Constants.KEY_PREFERENCE_VALUE)
            else -> throw IllegalArgumentException()
        }
//        viewModel.setBundleId(bundleId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
    }

    override fun onState(state: Nothing) = Unit

    override fun onEvent(event: Nothing) = Unit

    private fun setupToolbar() {
        with(binding) {
            toolbar.setNavigationOnClickListener { requireActivity().finish() }
        }
    }
}
