package com.infinum.sentinel.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RestrictTo
import androidx.core.app.ShareCompat
import com.google.android.material.shape.MaterialShapeDrawable
import com.infinum.sentinel.R
import com.infinum.sentinel.data.models.memory.formats.FormatType
import com.infinum.sentinel.data.sources.raw.ApplicationCollector
import com.infinum.sentinel.data.sources.raw.BasicCollector
import com.infinum.sentinel.data.sources.raw.DeviceCollector
import com.infinum.sentinel.data.sources.raw.PermissionsCollector
import com.infinum.sentinel.data.sources.raw.PreferencesCollector
import com.infinum.sentinel.databinding.SentinelFragmentBinding
import com.infinum.sentinel.extensions.toScissorsDrawable
import com.infinum.sentinel.ui.children.ApplicationFragment
import com.infinum.sentinel.ui.children.DeviceFragment
import com.infinum.sentinel.ui.children.PermissionsFragment
import com.infinum.sentinel.ui.children.PreferencesFragment
import com.infinum.sentinel.ui.children.SettingsFragment
import com.infinum.sentinel.ui.children.ToolsFragment
import com.infinum.sentinel.ui.formatters.FormattedStringBuilder
import com.infinum.sentinel.ui.shared.BaseFragment

@Suppress("TooManyFunctions")
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class SentinelFragment : BaseFragment<SentinelFragmentBinding>(), SentinelFeatures {

    companion object {
        val TAG: String = SentinelFragment::class.java.simpleName

        private const val SHARE_MIME_TYPE = "text/plain"
    }

    private var formatter: FormattedStringBuilder<*, *>? = null

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): SentinelFragmentBinding =
        SentinelFragmentBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUi()

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

        with(viewBinding) {
            toolbar.title = basicCollector.data.applicationName
            applicationIconView.background = basicCollector.data.applicationIcon
        }

        DependencyGraph.formats().load().observeForever { entity ->
            formatter = when (entity.type) {
                FormatType.PLAIN -> DependencyGraph.formatters().plain()
                FormatType.MARKDOWN -> DependencyGraph.formatters().markdown()
                FormatType.JSON -> DependencyGraph.formatters().json()
                FormatType.XML -> DependencyGraph.formatters().xml()
                FormatType.HTML -> DependencyGraph.formatters().html()
                else -> null
            }
        }

        tools()
    }

    private fun setupUi() {
        with(viewBinding) {
            toolbar.setNavigationOnClickListener { dismiss() }
            toolbar.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.share -> share()
                }
                true
            }
            contentLayout.background = MaterialShapeDrawable().toScissorsDrawable(
                context = requireContext(),
                color = R.color.sentinel_color_background,
                count = 12,
                height = R.dimen.sentinel_triangle_height
            )
            bottomAppBar.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.settings -> settings()
                    R.id.device -> device()
                    R.id.application -> application()
                    R.id.permissions -> permissions()
                    R.id.preferences -> preferences()
                }
                true
            }
            fab.setOnClickListener { tools() }
        }
    }

    override fun settings() {
        viewBinding.toolbar.subtitle = getString(R.string.sentinel_settings)
        showFragment(SettingsFragment.TAG)
    }

    override fun device() {
        viewBinding.toolbar.subtitle = getString(R.string.sentinel_device)
        showFragment(DeviceFragment.TAG)
    }

    override fun application() {
        viewBinding.toolbar.subtitle = getString(R.string.sentinel_application)
        showFragment(ApplicationFragment.TAG)
    }

    override fun permissions() {
        viewBinding.toolbar.subtitle = getString(R.string.sentinel_permissions)
        showFragment(PermissionsFragment.TAG)
    }

    override fun preferences() {
        viewBinding.toolbar.subtitle = getString(R.string.sentinel_preferences)
        showFragment(PreferencesFragment.TAG)
    }

    override fun tools() {
        viewBinding.toolbar.subtitle = getString(R.string.sentinel_tools)
        showFragment(ToolsFragment.TAG)
    }

    override fun share() {
        formatter?.format()?.let {
            ShareCompat.IntentBuilder.from(requireActivity())
                .setChooserTitle(R.string.sentinel_name)
                .setType(SHARE_MIME_TYPE)
                .setText(it)
                .startChooser()
        }
    }

    private fun showFragment(tag: String) {
        childFragmentManager.findFragmentByTag(tag)?.let {
            childFragmentManager.beginTransaction()
                .replace(viewBinding.fragmentContainer.id, it, tag)
                .commit()
        } ?: run {
            when (tag) {
                SettingsFragment.TAG -> SettingsFragment.newInstance()
                DeviceFragment.TAG -> DeviceFragment.newInstance()
                ApplicationFragment.TAG -> ApplicationFragment.newInstance()
                PermissionsFragment.TAG -> PermissionsFragment.newInstance()
                PreferencesFragment.TAG -> PreferencesFragment.newInstance()
                ToolsFragment.TAG -> ToolsFragment.newInstance()
                else -> null
            }?.let {
                childFragmentManager.beginTransaction()
                    .replace(viewBinding.fragmentContainer.id, it, tag)
                    .addToBackStack(null)
                    .commit()
            }
        }
    }
}
