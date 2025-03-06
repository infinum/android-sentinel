package com.infinum.sentinel.ui.main.preferences

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.annotation.RestrictTo
import androidx.core.os.bundleOf
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.infinum.sentinel.R
import com.infinum.sentinel.databinding.SentinelFragmentTargetedPreferencesBinding
import com.infinum.sentinel.extensions.viewModels
import com.infinum.sentinel.ui.main.preferences.all.AllPreferencesActivity
import com.infinum.sentinel.ui.main.preferences.editor.PreferenceEditorContract
import com.infinum.sentinel.ui.main.preferences.shared.adapter.PreferencesAdapter
import com.infinum.sentinel.ui.main.preferences.shared.model.flatten
import com.infinum.sentinel.ui.shared.base.BaseChildFragment
import com.infinum.sentinel.ui.shared.delegates.viewBinding

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class PreferencesFragment : BaseChildFragment<PreferencesState, PreferencesEvent>(
    R.layout.sentinel_fragment_targeted_preferences
) {

    companion object {
        fun newInstance(preferenceType: String) = PreferencesFragment().apply {
            arguments = bundleOf(
                EXTRA_PREFERENCE_TYPE to preferenceType
            )
        }

        const val TAG: String = "PreferencesFragment"

        const val TARGETED_PREFERENCES = "TARGETED_PREFERENCES"
        const val ALL_PREFERENCES = "ALL_PREFERENCES"

        private const val EXTRA_PREFERENCE_TYPE = "EXTRA_PREFERENCE_TYPE"
    }

    private lateinit var contract: ActivityResultLauncher<Unit>

    private lateinit var adapter: PreferencesAdapter

    override val binding: SentinelFragmentTargetedPreferencesBinding by viewBinding(
        SentinelFragmentTargetedPreferencesBinding::bind
    )

    override val viewModel: PreferencesViewModel by viewModels()

    private lateinit var preferenceType: String

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contract = registerForActivityResult(PreferenceEditorContract()) { shouldRefresh ->
            if (shouldRefresh) {
                viewModel.load(preferenceType == TARGETED_PREFERENCES)
            }
        }

        preferenceType = arguments?.getString(EXTRA_PREFERENCE_TYPE)
            ?: ("Suitable preference type for PreferencesFragment is not found")

        initUi()
        initAdapter()
        initListeners()
        initViewModel()
    }

    private fun initUi() {
        when (preferenceType) {
            TARGETED_PREFERENCES -> binding.apply {
                container.setPadding(0, 0, 0, 104.dpToPx())
                nestedScrollView.setPadding(0, 0, 0, 64.dpToPx())
                allPreferences.isVisible = true
                toolbar.isGone = true
            }

            ALL_PREFERENCES -> binding.apply {
                container.setPadding(0, 0, 0, 4.dpToPx())
                nestedScrollView.setPadding(0, 0, 0, 0)
                toolbar.isVisible = true
                toolbar.setNavigationOnClickListener { requireActivity().finish() }
                allPreferences.isGone = true
                applyWindowInsets()
            }

            else -> throw IllegalArgumentException("Suitable preference type for PreferencesFragment is not found")
        }
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

    private fun initViewModel() {
        viewModel.load(shouldFilter = preferenceType == TARGETED_PREFERENCES)
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

    @Suppress("NestedBlockDepth")
    override fun onState(state: PreferencesState) =
        when (state) {
            is PreferencesState.Data -> with(binding) {
                if (state.value.isEmpty()) {
                    recyclerView.isGone = true
                    if (preferenceType == TARGETED_PREFERENCES) emptyStateMessage.isVisible = true
                } else {
                    emptyStateMessage.isGone = true
                    recyclerView.isVisible = true
                    val flattenedPreferences = state.value.flatten()
                    adapter.submitList(flattenedPreferences)
                }
            }
        }

    override fun onEvent(event: PreferencesEvent) =
        when (event) {
            is PreferencesEvent.Cached -> {
                contract.launch(Unit)
            }
        }
}

internal fun Int.dpToPx(): Int = (this * Resources.getSystem().displayMetrics.density).toInt()
