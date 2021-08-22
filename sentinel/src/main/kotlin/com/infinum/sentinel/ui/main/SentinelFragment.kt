package com.infinum.sentinel.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.RestrictTo
import androidx.core.app.ShareCompat
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.ShapeAppearanceModel
import com.infinum.sentinel.R
import com.infinum.sentinel.databinding.SentinelFragmentBinding
import com.infinum.sentinel.extensions.toCradleDrawable
import com.infinum.sentinel.ui.main.application.ApplicationFragment
import com.infinum.sentinel.ui.main.device.DeviceFragment
import com.infinum.sentinel.ui.main.permissions.PermissionsFragment
import com.infinum.sentinel.ui.main.preferences.PreferencesFragment
import com.infinum.sentinel.ui.main.tools.ToolsFragment
import com.infinum.sentinel.ui.settings.SettingsActivity
import com.infinum.sentinel.ui.shared.base.BaseFragment
import com.infinum.sentinel.ui.shared.delegates.viewBinding
import com.infinum.sentinel.ui.shared.edgetreatment.ScissorsEdgeTreatment
import org.koin.androidx.viewmodel.ext.android.viewModel

@Suppress("TooManyFunctions")
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class SentinelFragment : BaseFragment(R.layout.sentinel_fragment) {

    companion object {
        val TAG: String = SentinelFragment::class.java.simpleName

        private const val SHARE_MIME_TYPE = "text/plain"
    }

    override val viewModel: SentinelViewModel by viewModel()

    override val binding: SentinelFragmentBinding by viewBinding(
        SentinelFragmentBinding::bind
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupContent()

        viewModel.checkIfEmulator()

        viewModel.applicationIconAndName {
            with(binding) {
                applicationIconView.background = it.first
                toolbar.subtitle = it.second
            }
        }

        showTools()
    }

    private fun setupToolbar() {
        with(binding) {
            applicationIconView.setOnClickListener { dismiss() }
            toolbar.setNavigationOnClickListener {
                startActivity(Intent(requireContext(), SettingsActivity::class.java))
            }
            toolbar.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.share -> {
                        viewModel.formatData {
                            ShareCompat.IntentBuilder(requireActivity())
                                .setChooserTitle(R.string.sentinel_name)
                                .setType(SHARE_MIME_TYPE)
                                .setText(it)
                                .startChooser()
                        }
                    }
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
            bottomNavigation.background = MaterialShapeDrawable().toCradleDrawable(
                context = requireContext(),
                color = R.color.sentinel_color_background,
                fabDiameter = resources
                    .getDimensionPixelSize(R.dimen.sentinel_cradle_diameter)
                    .toFloat(),
                fabCradleMargin = resources
                    .getDimensionPixelSize(R.dimen.sentinel_cradle_margin)
                    .toFloat(),
                fabCornerRadius = resources
                    .getDimensionPixelSize(R.dimen.sentinel_cradle_corner_radius)
                    .toFloat(),
                fabVerticalOffset = resources
                    .getDimensionPixelSize(R.dimen.sentinel_cradle_vertical_offset)
                    .toFloat()
            )
            bottomNavigation.elevation =
                resources.getDimensionPixelSize(R.dimen.sentinel_cradle_margin).toFloat() * 2
            bottomNavigation.setOnItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.device -> showFragment(DeviceFragment.TAG)
                    R.id.application -> showFragment(ApplicationFragment.TAG)
                    R.id.permissions -> showFragment(PermissionsFragment.TAG)
                    R.id.preferences -> showFragment(PreferencesFragment.TAG)
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
                PreferencesFragment.TAG -> PreferencesFragment.newInstance()
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
