package com.infinum.sentinel.ui

import android.os.Bundle
import android.view.View
import androidx.annotation.RestrictTo
import androidx.core.app.ShareCompat
import com.google.android.material.shape.MaterialShapeDrawable
import com.infinum.sentinel.R
import com.infinum.sentinel.data.models.memory.formats.FormatType
import com.infinum.sentinel.data.sources.local.room.repository.FormatsRepository
import com.infinum.sentinel.data.sources.raw.ApplicationCollector
import com.infinum.sentinel.ui.formatters.FormattedStringBuilder
import com.infinum.sentinel.ui.formatters.HtmlStringBuilder
import com.infinum.sentinel.ui.formatters.JsonStringBuilder
import com.infinum.sentinel.ui.formatters.MarkdownStringBuilder
import com.infinum.sentinel.ui.formatters.PlainStringBuilder
import com.infinum.sentinel.ui.formatters.XmlStringBuilder
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
import com.infinum.sentinel.ui.shared.BaseFragment
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class SentinelFragment : BaseFragment<SentinelFragmentBinding>(), SentinelFeatures {

    companion object {
        val TAG: String = SentinelFragment::class.java.simpleName

        private const val SHARE_MIME_TYPE = "text/plain"
    }

    private var formatter: FormattedStringBuilder? = null

    private val plainFormatter: PlainStringBuilder by inject()
    private val markdownFormatter: MarkdownStringBuilder by inject()
    private val jsonFormatter: JsonStringBuilder by inject()
    private val xmlFormatter: XmlStringBuilder by inject()
    private val htmlFormatter: HtmlStringBuilder by inject()

    override fun provideViewBinding(): SentinelFragmentBinding =
        SentinelFragmentBinding.inflate(layoutInflater)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupContent()
        setupBottomAppBar()

        val basicCollector: BasicCollector = get()
        val applicationCollector: ApplicationCollector = get()
        val deviceCollector: DeviceCollector = get()
        val permissionsCollector: PermissionsCollector = get()
        val preferencesCollector: PreferencesCollector = get()

        basicCollector.collect()
        applicationCollector.collect()
        deviceCollector.collect()
        permissionsCollector.collect()
        preferencesCollector.collect()

        basicCollector.present().let {
            with(viewBinding) {
                toolbar.title = basicCollector.data.applicationName
                applicationIconView.background = basicCollector.data.applicationIcon
            }
        }

        tools()

        FormatsRepository.load().observeForever { entity ->
            formatter = when (entity.type) {
                FormatType.PLAIN -> plainFormatter
                FormatType.MARKDOWN -> markdownFormatter
                FormatType.JSON -> jsonFormatter
                FormatType.XML -> xmlFormatter
                FormatType.HTML -> htmlFormatter
                else -> null
            }
        }
    }

    private fun setupToolbar() {
        with(viewBinding) {
            toolbar.setNavigationOnClickListener { dismiss() }
            toolbar.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.share -> share()
                }
                true
            }
        }
    }

    private fun setupContent() {
        with(viewBinding) {
            contentLayout.background = MaterialShapeDrawable().toScissorsDrawable(
                context = requireContext(),
                color = R.color.sentinel_color_background,
                count = 12,
                height = R.dimen.sentinel_triangle_height
            )
        }
    }

    private fun setupBottomAppBar() {
        with(viewBinding) {
            bottomAppBar.setNavigationOnClickListener { settings() }
            bottomAppBar.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
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
        with(viewBinding) {
            toolbar.subtitle = getString(R.string.sentinel_settings)
            childFragmentManager.beginTransaction()
                .apply {
                    childFragmentManager.findFragmentByTag(SettingsFragment.TAG)?.let {
                        this.replace(fragmentContainer.id, it)
                    } ?: run {
                        this.replace(
                            fragmentContainer.id,
                            SettingsFragment.newInstance(),
                            SettingsFragment.TAG
                        )
                        this.addToBackStack(SettingsFragment.TAG)
                    }
                }.commit()
        }
    }

    override fun device() {
        with(viewBinding) {
            toolbar.subtitle = getString(R.string.sentinel_device)
            childFragmentManager.beginTransaction()
                .apply {
                    childFragmentManager.findFragmentByTag(DeviceFragment.TAG)?.let {
                        this.replace(fragmentContainer.id, it)
                    } ?: run {
                        this.replace(
                            fragmentContainer.id,
                            DeviceFragment.newInstance(),
                            DeviceFragment.TAG
                        )
                        this.addToBackStack(DeviceFragment.TAG)
                    }
                }.commit()
        }
    }

    override fun application() {
        with(viewBinding) {
            toolbar.subtitle = getString(R.string.sentinel_application)
            childFragmentManager.beginTransaction()
                .apply {
                    childFragmentManager.findFragmentByTag(ApplicationFragment.TAG)?.let {
                        this.replace(fragmentContainer.id, it)
                    } ?: run {
                        this.replace(
                            fragmentContainer.id,
                            ApplicationFragment.newInstance(),
                            ApplicationFragment.TAG
                        )
                        this.addToBackStack(ApplicationFragment.TAG)
                    }
                }.commit()
        }
    }

    override fun permissions() {
        with(viewBinding) {
            toolbar.subtitle = getString(R.string.sentinel_permissions)
            childFragmentManager.beginTransaction()
                .apply {
                    childFragmentManager.findFragmentByTag(PermissionsFragment.TAG)?.let {
                        this.replace(fragmentContainer.id, it)
                    } ?: run {
                        this.replace(
                            fragmentContainer.id,
                            PermissionsFragment.newInstance(),
                            PermissionsFragment.TAG
                        )
                        this.addToBackStack(PermissionsFragment.TAG)
                    }
                }.commit()
        }
    }

    override fun preferences() {
        with(viewBinding) {
            toolbar.subtitle = getString(R.string.sentinel_preferences)
            childFragmentManager.beginTransaction()
                .apply {
                    childFragmentManager.findFragmentByTag(PreferencesFragment.TAG)?.let {
                        this.replace(fragmentContainer.id, it)
                    } ?: run {
                        this.replace(
                            fragmentContainer.id,
                            PreferencesFragment.newInstance(),
                            PreferencesFragment.TAG
                        )
                        this.addToBackStack(PreferencesFragment.TAG)
                    }
                }.commit()
        }
    }

    override fun tools() {
        with(viewBinding) {
            toolbar.subtitle = getString(R.string.sentinel_tools)
            childFragmentManager.beginTransaction()
                .apply {
                    childFragmentManager.findFragmentByTag(ToolsFragment.TAG)?.let {
                        this.replace(fragmentContainer.id, it)
                    } ?: run {
                        this.replace(
                            fragmentContainer.id,
                            ToolsFragment.newInstance(),
                            ToolsFragment.TAG
                        )
                        this.addToBackStack(ToolsFragment.TAG)
                    }
                }.commit()
        }
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
}
