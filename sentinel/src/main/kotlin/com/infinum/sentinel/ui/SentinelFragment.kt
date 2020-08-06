package com.infinum.sentinel.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.RestrictTo
import androidx.core.app.ShareCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.shape.MaterialShapeDrawable
import com.infinum.sentinel.R
import com.infinum.sentinel.data.models.memory.formats.FormatType
import com.infinum.sentinel.data.sources.raw.ApplicationCollector
import com.infinum.sentinel.data.sources.raw.BasicCollector
import com.infinum.sentinel.data.sources.raw.DeviceCollector
import com.infinum.sentinel.data.sources.raw.PermissionsCollector
import com.infinum.sentinel.data.sources.raw.PreferencesCollector
import com.infinum.sentinel.databinding.SentinelFragmentBinding
import com.infinum.sentinel.extensions.toCradleDrawable
import com.infinum.sentinel.extensions.toScissorsDrawable
import com.infinum.sentinel.ui.children.ApplicationFragment
import com.infinum.sentinel.ui.children.DeviceFragment
import com.infinum.sentinel.ui.children.PermissionsFragment
import com.infinum.sentinel.ui.children.PreferencesFragment
import com.infinum.sentinel.ui.children.ToolsFragment
import com.infinum.sentinel.ui.settings.SettingsActivity
import com.infinum.sentinel.ui.shared.BaseFragment
import com.infinum.sentinel.ui.shared.viewBinding
import kotlinx.coroutines.launch

@Suppress("TooManyFunctions")
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class SentinelFragment : BaseFragment(R.layout.sentinel_fragment) {

    companion object {
        val TAG: String = SentinelFragment::class.java.simpleName

        private const val SHARE_MIME_TYPE = "text/plain"
    }

    override val binding: SentinelFragmentBinding by viewBinding(
        SentinelFragmentBinding::bind
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupContent()

        val basicCollector: BasicCollector = DependencyGraph.collectors().basic()
        val applicationCollector: ApplicationCollector = DependencyGraph.collectors().application()
        val deviceCollector: DeviceCollector = DependencyGraph.collectors().device()
        val permissionsCollector: PermissionsCollector = DependencyGraph.collectors().permissions()
        val preferencesCollector: PreferencesCollector = DependencyGraph.collectors().preferences()

        basicCollector.collect()
        applicationCollector.collect()
        deviceCollector.collect()
        permissionsCollector.collect()
        preferencesCollector.collect()

        with(binding) {
            toolbar.subtitle = basicCollector.data.applicationName
            applicationIconView.setImageDrawable(basicCollector.data.applicationIcon)
            applicationIconView.setOnClickListener { dismiss() }
        }

        showTools()
    }

    private fun setupToolbar() {
        with(binding) {
            toolbar.setNavigationOnClickListener {
                startActivity(Intent(requireContext(), SettingsActivity::class.java))
            }
            toolbar.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.share -> {
                        lifecycleScope.launch {
                            when (DependencyGraph.formats().load().type) {
                                FormatType.PLAIN -> DependencyGraph.formatters().plain()
                                FormatType.MARKDOWN -> DependencyGraph.formatters().markdown()
                                FormatType.JSON -> DependencyGraph.formatters().json()
                                FormatType.XML -> DependencyGraph.formatters().xml()
                                FormatType.HTML -> DependencyGraph.formatters().html()
                                else -> null
                            }?.format().let { formattedText ->
                                ShareCompat.IntentBuilder.from(requireActivity())
                                    .setChooserTitle(R.string.sentinel_name)
                                    .setType(SHARE_MIME_TYPE)
                                    .setText(formattedText)
                                    .startChooser()
                            }
                        }
                    }
                }
                true
            }
        }
    }

    private fun setupContent() {
        with(binding) {
            contentLayout.background = MaterialShapeDrawable().toScissorsDrawable(
                context = requireContext(),
                color = R.color.sentinel_color_background,
                count = 12,
                height = R.dimen.sentinel_triangle_height
            )
            bottomNavigation.background = MaterialShapeDrawable().toCradleDrawable(
                context = requireContext(),
                color = R.color.sentinel_color_background,
                fabDiameter = resources.getDimensionPixelSize(R.dimen.sentinel_cradle_diameter)
                    .toFloat(),
                fabCradleMargin = resources.getDimensionPixelSize(R.dimen.sentinel_cradle_margin)
                    .toFloat(),
                fabCornerRadius = resources.getDimensionPixelSize(R.dimen.sentinel_cradle_corner_radius)
                    .toFloat(),
                fabVerticalOffset = resources.getDimensionPixelSize(R.dimen.sentinel_cradle_vertical_offset)
                    .toFloat()
            )
            bottomNavigation.elevation =
                resources.getDimensionPixelSize(R.dimen.sentinel_cradle_margin).toFloat() * 2
            bottomNavigation.setOnNavigationItemSelectedListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.device -> showFragment(DeviceFragment.TAG)
                    R.id.application -> showFragment(ApplicationFragment.TAG)
                    R.id.permissions -> showFragment(PermissionsFragment.TAG)
                    R.id.preferences -> showFragment(PreferencesFragment.TAG)
                }
                true
            }
            bottomNavigation.setOnNavigationItemReselectedListener { Unit }
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
