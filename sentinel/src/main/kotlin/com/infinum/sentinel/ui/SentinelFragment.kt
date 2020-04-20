package com.infinum.sentinel.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ShareCompat
import com.google.android.material.shape.MaterialShapeDrawable
import com.infinum.sentinel.R
import com.infinum.sentinel.data.models.memory.formats.FormatType
import com.infinum.sentinel.data.models.raw.AppInfo
import com.infinum.sentinel.data.sources.local.room.repository.FormatsRepository
import com.infinum.sentinel.data.sources.memory.FormattedStringBuilder
import com.infinum.sentinel.data.sources.memory.HtmlStringBuilder
import com.infinum.sentinel.data.sources.memory.JsonStringBuilder
import com.infinum.sentinel.data.sources.memory.MarkdownStringBuilder
import com.infinum.sentinel.data.sources.memory.PlainStringBuilder
import com.infinum.sentinel.data.sources.memory.XmlStringBuilder
import com.infinum.sentinel.data.sources.raw.DataSource
import com.infinum.sentinel.databinding.SentinelFragmentBinding
import com.infinum.sentinel.extensions.toScissorsDrawable
import com.infinum.sentinel.ui.children.ApplicationFragment
import com.infinum.sentinel.ui.children.DeviceFragment
import com.infinum.sentinel.ui.children.PermissionsFragment
import com.infinum.sentinel.ui.children.SettingsFragment
import com.infinum.sentinel.ui.children.ToolsFragment
import com.infinum.sentinel.ui.shared.BaseFragment

class SentinelFragment : BaseFragment() {

    companion object {
        val TAG: String = SentinelFragment::class.java.simpleName

        private const val SHARE_MIME_TYPE = "text/plain"
    }

    private var viewBinding: SentinelFragmentBinding? = null

    private var formatter: FormattedStringBuilder? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = SentinelFragmentBinding.inflate(inflater, container, false)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding?.let {
            it.toolbar.setNavigationOnClickListener { dismiss() }
            it.toolbar.title = DataSource.applicationData[AppInfo.NAME]
            it.toolbar.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.share -> showShare()
                }
                true
            }

            val applicationIcon = DataSource.applicationIcon

            it.applicationIconView.background = applicationIcon

            it.contentLayout.background = MaterialShapeDrawable().toScissorsDrawable(
                context = requireContext(),
                color = R.color.sentinel_color_background,
                count = 12,
                height = R.dimen.sentinel_triangle_height
            )

            it.bottomAppBar.setNavigationOnClickListener { showSettings() }
            it.bottomAppBar.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.device -> showDevice()
                    R.id.application -> showApplication()
                    R.id.permissions -> showPermissions()
                }
                true
            }
            it.fab.setOnClickListener { showTools() }

            showTools()

            FormatsRepository.load().observeForever { entity ->
                formatter = when (entity.type) {
                    FormatType.PLAIN -> PlainStringBuilder()
                    FormatType.MARKDOWN -> MarkdownStringBuilder()
                    FormatType.JSON -> JsonStringBuilder()
                    FormatType.XML -> XmlStringBuilder()
                    FormatType.HTML -> HtmlStringBuilder()
                    else -> null
                }
            }
        }
    }

    override fun onDestroy() =
        super.onDestroy().run {
            viewBinding = null
        }

    override fun onDetach() =
        super.onDetach().run {
            requireActivity().finish()
        }

    private fun showSettings() {
        viewBinding?.let { binding ->
            binding.toolbar.subtitle = getString(R.string.sentinel_settings)
            childFragmentManager.beginTransaction()
                .apply {
                    childFragmentManager.findFragmentByTag(SettingsFragment.TAG)?.let {
                        this.replace(binding.fragmentContainer.id, it)
                    } ?: run {
                        this.replace(
                            binding.fragmentContainer.id,
                            SettingsFragment.newInstance(),
                            SettingsFragment.TAG
                        )
                        this.addToBackStack(SettingsFragment.TAG)
                    }
                }.commit()
        }
    }

    private fun showDevice() {
        viewBinding?.let { binding ->
            binding.toolbar.subtitle = getString(R.string.sentinel_device)
            childFragmentManager.beginTransaction()
                .apply {
                    childFragmentManager.findFragmentByTag(DeviceFragment.TAG)?.let {
                        this.replace(binding.fragmentContainer.id, it)
                    } ?: run {
                        this.replace(
                            binding.fragmentContainer.id,
                            DeviceFragment.newInstance(),
                            DeviceFragment.TAG
                        )
                        this.addToBackStack(DeviceFragment.TAG)
                    }
                }.commit()
        }
    }

    private fun showApplication() {
        viewBinding?.let { binding ->
            binding.toolbar.subtitle = getString(R.string.sentinel_application)
            childFragmentManager.beginTransaction()
                .apply {
                    childFragmentManager.findFragmentByTag(ApplicationFragment.TAG)?.let {
                        this.replace(binding.fragmentContainer.id, it)
                    } ?: run {
                        this.replace(
                            binding.fragmentContainer.id,
                            ApplicationFragment.newInstance(),
                            ApplicationFragment.TAG
                        )
                        this.addToBackStack(ApplicationFragment.TAG)
                    }
                }.commit()
        }
    }

    private fun showPermissions() {
        viewBinding?.let { binding ->
            binding.toolbar.subtitle = getString(R.string.sentinel_permissions)
            childFragmentManager.beginTransaction()
                .apply {
                    childFragmentManager.findFragmentByTag(PermissionsFragment.TAG)?.let {
                        this.replace(binding.fragmentContainer.id, it)
                    } ?: run {
                        this.replace(
                            binding.fragmentContainer.id,
                            PermissionsFragment.newInstance(),
                            PermissionsFragment.TAG
                        )
                        this.addToBackStack(PermissionsFragment.TAG)
                    }
                }.commit()
        }
    }

    private fun showTools() {
        viewBinding?.let { binding ->
            binding.toolbar.subtitle = getString(R.string.sentinel_tools)
            childFragmentManager.beginTransaction()
                .apply {
                    childFragmentManager.findFragmentByTag(ToolsFragment.TAG)?.let {
                        this.replace(binding.fragmentContainer.id, it)
                    } ?: run {
                        this.replace(
                            binding.fragmentContainer.id,
                            ToolsFragment.newInstance(),
                            ToolsFragment.TAG
                        )
                        this.addToBackStack(ToolsFragment.TAG)
                    }
                }.commit()
        }
    }

    private fun showShare() =
        formatter?.format()?.let {
            ShareCompat.IntentBuilder.from(requireActivity())
                .setChooserTitle(R.string.sentinel_name)
                .setType(SHARE_MIME_TYPE)
                .setText(it)
                .startChooser()
        }
}
