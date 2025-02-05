package com.infinum.sentinel.ui.main.preferences.targeted

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RestrictTo
import com.infinum.sentinel.R
import com.infinum.sentinel.data.models.raw.PreferencesData
import com.infinum.sentinel.databinding.SentinelFragmentTargetedPreferencesBinding
import com.infinum.sentinel.databinding.SentinelViewItemPreferenceBinding
import com.infinum.sentinel.databinding.SentinelViewItemTextBinding
import com.infinum.sentinel.extensions.copyToClipboard
import com.infinum.sentinel.extensions.viewModels
import com.infinum.sentinel.ui.main.preferences.all.AllPreferencesActivity
import com.infinum.sentinel.ui.main.preferences.editor.PreferenceEditorContract
import com.infinum.sentinel.ui.shared.base.BaseChildFragment
import com.infinum.sentinel.ui.shared.delegates.viewBinding

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class TargetedPreferencesFragment :
    BaseChildFragment<TargetedPreferencesState, TargetedPreferencesEvent>(R.layout.sentinel_fragment_targeted_preferences) {

    companion object {
        fun newInstance() = TargetedPreferencesFragment()
        const val TAG: String = "TargetedPreferencesFragment"
    }

    private lateinit var contract: ActivityResultLauncher<Unit>

    override val binding: SentinelFragmentTargetedPreferencesBinding by viewBinding(
        SentinelFragmentTargetedPreferencesBinding::bind
    )

    override val viewModel: TargetedPreferencesViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contract = registerForActivityResult(PreferenceEditorContract()) { shouldRefresh ->
            if (shouldRefresh) {
                viewModel.data()
            }
        }

        initListeners()
    }

    private fun initListeners() {
        binding.apply {
            allPreferences.setOnClickListener {
                startActivity(Intent(context, AllPreferencesActivity::class.java))
            }
        }
    }

    override fun onDestroyView() {
        contract.unregister()
        super.onDestroyView()
    }

    override fun onState(state: TargetedPreferencesState) =
        when (state) {
            is TargetedPreferencesState.Data -> with(binding) {
                contentLayout.removeAllViews()
                state.value.forEach {
                    contentLayout.addView(createItemView(it))
                }
            }
        }

    override fun onEvent(event: TargetedPreferencesEvent) =
        when (event) {
            is TargetedPreferencesEvent.Cached -> {
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
