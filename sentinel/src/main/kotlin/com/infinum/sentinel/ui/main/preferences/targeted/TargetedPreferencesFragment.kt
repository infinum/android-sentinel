package com.infinum.sentinel.ui.main.preferences.targeted

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RestrictTo
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.infinum.sentinel.R
import com.infinum.sentinel.databinding.SentinelFragmentTargetedPreferencesBinding
import com.infinum.sentinel.extensions.viewModels
import com.infinum.sentinel.ui.main.preferences.all.AllPreferencesActivity
import com.infinum.sentinel.ui.main.preferences.shared.adapter.PreferencesAdapter
import com.infinum.sentinel.ui.main.preferences.editor.PreferenceEditorContract
import com.infinum.sentinel.ui.main.preferences.shared.model.flatten
import com.infinum.sentinel.ui.shared.base.BaseChildFragment
import com.infinum.sentinel.ui.shared.delegates.viewBinding

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class TargetedPreferencesFragment : BaseChildFragment<TargetedPreferencesState, TargetedPreferencesEvent>(
    R.layout.sentinel_fragment_targeted_preferences
) {

    companion object {
        fun newInstance() = TargetedPreferencesFragment()
        const val TAG: String = "TargetedPreferencesFragment"
    }

    private lateinit var contract: ActivityResultLauncher<Unit>

    private lateinit var adapter: PreferencesAdapter

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

        initAdapter()
        initListeners()
    }

    private fun initAdapter() {
        adapter = PreferencesAdapter(
            onSortClicked = { name -> viewModel.onSortClicked(name) },
            onHideExpandClick = { name -> viewModel.onHideExpandClicked(name) },
            onPreferenceClicked = { name, tuple -> viewModel.cache(name, tuple) }
        )
        binding.recyclerView.adapter = adapter
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

                if (state.value.isEmpty()) {
                    recyclerView.isGone = true
                    emptyStateMessage.isVisible = true
                } else {
                    emptyStateMessage.isGone = true
                    recyclerView.isVisible = true
                    val flattenedPreferences = state.value.flatten()
                    adapter.submitList(flattenedPreferences)
                }
            }
        }

    override fun onEvent(event: TargetedPreferencesEvent) =
        when (event) {
            is TargetedPreferencesEvent.Cached -> {
                contract.launch(Unit)
            }
        }
}
