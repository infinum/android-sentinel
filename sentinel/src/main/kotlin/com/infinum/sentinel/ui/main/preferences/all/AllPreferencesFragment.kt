package com.infinum.sentinel.ui.main.preferences.all

import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RestrictTo
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.infinum.sentinel.R
import com.infinum.sentinel.data.models.raw.PreferencesData
import com.infinum.sentinel.databinding.SentinelFragmentAllPreferencesBinding
import com.infinum.sentinel.databinding.SentinelViewItemPreferenceBinding
import com.infinum.sentinel.databinding.SentinelViewItemTextBinding
import com.infinum.sentinel.extensions.copyToClipboard
import com.infinum.sentinel.extensions.viewModels
import com.infinum.sentinel.ui.main.preferences.editor.PreferenceEditorContract
import com.infinum.sentinel.ui.shared.base.BaseChildFragment
import com.infinum.sentinel.ui.shared.delegates.viewBinding

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class AllPreferencesFragment :
    BaseChildFragment<AllPreferencesState, AllPreferencesEvent>(R.layout.sentinel_fragment_all_preferences) {

    companion object {
        fun newInstance() = AllPreferencesFragment()
        const val TAG: String = "AllPreferencesFragment"
    }

    private lateinit var contract: ActivityResultLauncher<Unit>

    override val binding: SentinelFragmentAllPreferencesBinding by viewBinding(
        SentinelFragmentAllPreferencesBinding::bind
    )

    override val viewModel: AllPreferencesViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        applyWindowInsets()
        initToolbar()

        contract = registerForActivityResult(PreferenceEditorContract()) { shouldRefresh ->
            if (shouldRefresh) {
                viewModel.data()
            }
        }
    }

    private fun initToolbar() {
        binding.toolbar.setNavigationOnClickListener { requireActivity().finish() }
    }

    private fun applyWindowInsets() {
        view?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { _, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

                binding.contentLayout.setPadding(0, 0, 0, systemBars.bottom)

                insets
            }
        }
    }

    override fun onDestroyView() {
        contract.unregister()
        super.onDestroyView()
    }

    override fun onState(state: AllPreferencesState) =
        when (state) {
            is AllPreferencesState.Data -> with(binding) {
                contentLayout.removeAllViews()
                state.value.forEach {
                    contentLayout.addView(createItemView(it))
                }
            }
        }

    override fun onEvent(event: AllPreferencesEvent) =
        when (event) {
            is AllPreferencesEvent.Cached -> {
                contract.launch(Unit)
            }
        }

    private fun createItemView(data: PreferencesData): View =
        SentinelViewItemPreferenceBinding.inflate(layoutInflater, binding.contentLayout, false)
            .apply {
                nameView.text = data.name
                sortImageView.setOnClickListener {
                    viewModel.onSortClicked(data)
                }
                hideExpandImageView.setOnClickListener {
                    viewModel.onHideExpandClicked(data)
                }

                if (data.isExpanded) {
                    prefsLayout.visibility = View.VISIBLE
                    sortImageView.visibility = View.VISIBLE
                    hideExpandImageView.setImageResource(R.drawable.sentinel_ic_minus)
                    showPreferenceData(data)
                } else {
                    prefsLayout.visibility = View.GONE
                    sortImageView.visibility = View.GONE
                    hideExpandImageView.setImageResource(R.drawable.sentinel_ic_plus)
                }
            }.root

    private fun SentinelViewItemPreferenceBinding.showPreferenceData(data: PreferencesData) {
        data.values.forEach { (preferenceType, label, value) ->
            prefsLayout.addView(
                SentinelViewItemTextBinding.inflate(layoutInflater, prefsLayout, false)
                    .apply {
                        labelView.isAllCaps = false
                        labelView.text = label
                        valueView.text = value.toString()
                        root.setOnClickListener { _ ->
                            viewModel.cache(
                                name = data.name,
                                tuple = Triple(preferenceType, label, value)
                            )
                        }
                        root.setOnLongClickListener {
                            it.context.copyToClipboard(
                                key = label,
                                value = value.toString()
                            )
                        }
                    }.root
            )
        }
    }
}