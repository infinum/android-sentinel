package com.infinum.sentinel.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.RestrictTo
import androidx.core.app.ShareCompat
import com.google.android.material.shape.ShapeAppearanceModel
import com.infinum.sentinel.R
import com.infinum.sentinel.databinding.SentinelFragmentBinding
import com.infinum.sentinel.extensions.shareText
import com.infinum.sentinel.extensions.viewModels
import com.infinum.sentinel.ui.main.application.ApplicationFragment
import com.infinum.sentinel.ui.main.device.DeviceFragment
import com.infinum.sentinel.ui.main.permissions.PermissionsFragment
import com.infinum.sentinel.ui.main.preferences.targeted.TargetedPreferencesFragment
import com.infinum.sentinel.ui.main.tools.ToolsFragment
import com.infinum.sentinel.ui.settings.SettingsActivity
import com.infinum.sentinel.ui.shared.base.BaseFragment
import com.infinum.sentinel.ui.shared.delegates.viewBinding
import com.infinum.sentinel.ui.shared.edgetreatment.ScissorsEdgeTreatment

@Suppress("TooManyFunctions")
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class SentinelFragment :
    BaseFragment<SentinelState, SentinelEvent>(R.layout.sentinel_fragment) {

    companion object {
        const val TAG: String = "SentinelFragment"
    }

    override val viewModel: SentinelViewModel by viewModels()

    override val binding: SentinelFragmentBinding by viewBinding(
        SentinelFragmentBinding::bind
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupContent()

        viewModel.checkIfEmulator()

        viewModel.applicationIconAndName()

        showTools()
    }

    override fun onState(state: SentinelState) =
        when (state) {
            is SentinelState.ApplicationIconAndName -> {
                with(binding) {
                    applicationIconView.background = state.icon
                    toolbar.subtitle = state.name
                }
            }
        }

    override fun onEvent(event: SentinelEvent) =
        when (event) {
            is SentinelEvent.Formatted ->
                ShareCompat.IntentBuilder(requireActivity()).shareText(event.value)
        }

    private fun setupToolbar() {
        with(binding) {
            applicationIconView.setOnClickListener { dismiss() }
            toolbar.setNavigationOnClickListener {
                startActivity(Intent(requireContext(), SettingsActivity::class.java))
            }
            toolbar.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.share -> viewModel.formatData()
                }
                true
            }
        }
    }

    private fun setupContent() {
        with(binding) {
            contentLayout.shapeAppearanceModel = ShapeAppearanceModel.Builder()
                .setTopEdge(
                    ScissorsEdgeTreatment(
                        resources.getInteger(R.integer.sentinel_scissors_top_count),
                        contentLayout
                            .context
                            .resources
                            .getDimensionPixelSize(R.dimen.sentinel_triangle_height)
                            .toFloat(),
                        true
                    )
                )
                .build()
            bottomNavigation.elevation =
                resources.getDimensionPixelSize(R.dimen.sentinel_cradle_margin).toFloat() * 2
            bottomNavigation.setOnItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.device -> showFragment(DeviceFragment.TAG)
                    R.id.application -> showFragment(ApplicationFragment.TAG)
                    R.id.permissions -> showFragment(PermissionsFragment.TAG)
                    R.id.preferences -> showFragment(TargetedPreferencesFragment.TAG)
                }
                true
            }
            fab.setOnClickListener { showTools() }
        }
    }

    private fun showTools() {
        binding.bottomNavigation.selectedItemId = R.id.blank
        showFragment(ToolsFragment.TAG)
    }

    private fun showFragment(tag: String) {
        childFragmentManager.findFragmentByTag(tag)?.let {
            childFragmentManager.beginTransaction()
                .replace(binding.fragmentContainer.id, it, tag)
                .commit()
        } ?: run {
            when (tag) {
                DeviceFragment.TAG -> DeviceFragment.newInstance()
                ApplicationFragment.TAG -> ApplicationFragment.newInstance()
                PermissionsFragment.TAG -> PermissionsFragment.newInstance()
                TargetedPreferencesFragment.TAG -> TargetedPreferencesFragment.newInstance()
                ToolsFragment.TAG -> ToolsFragment.newInstance()
                else -> null
            }?.let {
                childFragmentManager.beginTransaction()
                    .replace(binding.fragmentContainer.id, it, tag)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }
}
