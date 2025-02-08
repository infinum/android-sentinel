package com.infinum.sentinel.ui.main.preferences.all

import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RestrictTo
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.infinum.sentinel.R
import com.infinum.sentinel.databinding.SentinelFragmentAllPreferencesBinding
import com.infinum.sentinel.extensions.viewModels
import com.infinum.sentinel.ui.main.preferences.shared.adapter.PreferencesAdapter
import com.infinum.sentinel.ui.main.preferences.editor.PreferenceEditorContract
import com.infinum.sentinel.ui.main.preferences.shared.model.flatten
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

    private lateinit var adapter: PreferencesAdapter

    override val binding: SentinelFragmentAllPreferencesBinding by viewBinding(
        SentinelFragmentAllPreferencesBinding::bind
    )

    override val viewModel: AllPreferencesViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        applyWindowInsets()

        contract = registerForActivityResult(PreferenceEditorContract()) { shouldRefresh ->
            if (shouldRefresh) {
                viewModel.data()
            }
        }

        initToolbar()
        initAdapter()
    }

    private fun initAdapter() {
        adapter = PreferencesAdapter(
            onSortClicked = { name -> viewModel.onSortClicked(name) },
            onHideExpandClick = { name -> viewModel.onHideExpandClicked(name) },
            onPreferenceClicked = { name, tuple -> viewModel.cache(name, tuple) }
        )
        binding.recyclerView.adapter = adapter
    }

    private fun initToolbar() {
        binding.toolbar.setNavigationOnClickListener { requireActivity().finish() }
    }

    private fun applyWindowInsets() {
        view?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { _, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

                binding.recyclerView.setPadding(0, 0, 0, systemBars.bottom)

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
                val flattenedPreferences = state.value.flatten()
                adapter.submitList(flattenedPreferences)
            }
        }

    override fun onEvent(event: AllPreferencesEvent) =
        when (event) {
            is AllPreferencesEvent.Cached -> {
                contract.launch(Unit)
            }
        }
}
