package com.infinum.sentinel.ui.deeplinks

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.View
import androidx.annotation.RestrictTo
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ShareCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.infinum.sentinel.R
import com.infinum.sentinel.databinding.SentinelFragmentDeepLinksBinding
import com.infinum.sentinel.domain.deeplinks.models.BaseLinkOrigin
import com.infinum.sentinel.domain.deeplinks.models.LaunchMode
import com.infinum.sentinel.domain.deeplinks.models.LinkType
import com.infinum.sentinel.domain.deeplinks.models.VerificationStatus
import com.infinum.sentinel.domain.deeplinks.overview.DeepLinksNotice
import com.infinum.sentinel.domain.deeplinks.overview.DeepLinksOverview
import com.infinum.sentinel.extensions.copyToClipboard
import com.infinum.sentinel.extensions.shareText
import com.infinum.sentinel.extensions.viewModels
import com.infinum.sentinel.ui.deeplinks.breakdown.LinkBreakdownActivity
import com.infinum.sentinel.ui.deeplinks.dialogs.ConfirmationDialogFragment
import com.infinum.sentinel.ui.deeplinks.editor.VariantEditorActivity
import com.infinum.sentinel.ui.deeplinks.editor.VariantEditorFocus
import com.infinum.sentinel.ui.deeplinks.editor.VariantEditorTarget
import com.infinum.sentinel.ui.deeplinks.launch.DeepLinkLauncher
import com.infinum.sentinel.ui.deeplinks.launch.LaunchModePopup
import com.infinum.sentinel.ui.deeplinks.launch.LaunchResult
import com.infinum.sentinel.ui.deeplinks.list.DeepLinksAdapter
import com.infinum.sentinel.ui.deeplinks.list.DeepLinksDividerDecoration
import com.infinum.sentinel.ui.deeplinks.list.DeepLinksItem
import com.infinum.sentinel.ui.deeplinks.list.DeepLinksListener
import com.infinum.sentinel.ui.deeplinks.list.flatten
import com.infinum.sentinel.ui.deeplinks.verification.VerificationStatusDialogFragment
import com.infinum.sentinel.ui.shared.base.BaseChildFragment
import com.infinum.sentinel.ui.shared.delegates.viewBinding
import com.infinum.sentinel.ui.shared.edgefactories.bounce.BounceEdgeEffectFactory

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class DeepLinksFragment :
    BaseChildFragment<DeepLinksState, DeepLinksEvent>(R.layout.sentinel_fragment_deep_links),
    DeepLinksListener {
    companion object {
        fun newInstance() = DeepLinksFragment()

        const val TAG: String = "DeepLinksFragment"

        private const val REQUEST_DELETE_VARIANT = "REQUEST_DELETE_VARIANT"
        private const val REQUEST_DELETE_ALL = "REQUEST_DELETE_ALL"

        /** A launch failure reason can be long, so its Snackbar gets more than the default two lines. */
        private const val LAUNCH_FAILED_MAX_LINES = 5
    }

    override val binding: SentinelFragmentDeepLinksBinding by viewBinding(
        SentinelFragmentDeepLinksBinding::bind,
    )

    override val viewModel: DeepLinksViewModel by viewModels()

    private val adapter = DeepLinksAdapter(listener = this)

    private var variantCount = 0

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        setupDialogResults()
    }

    override fun onResume() {
        super.onResume()
        // Coming back from system settings must update the statuses without reopening the tool (FR-43).
        viewModel.refreshVerification()
    }

    override fun onState(state: DeepLinksState) {
        when (state) {
            is DeepLinksState.Data -> {
                variantCount = state.variantCount
                binding.toolbar.menu
                    .findItem(R.id.deleteAll)
                    ?.isEnabled = state.variantCount > 0
                binding.toolbar.menu
                    .findItem(R.id.openLinkSettings)
                    ?.isVisible = state.isLinkSettingsAvailable
                showOverview(state.overview)
            }
        }
    }

    override fun onEvent(event: DeepLinksEvent) {
        when (event) {
            DeepLinksEvent.ChangeFailed -> showSnackbar(getString(R.string.sentinel_deep_links_change_failed))
        }
    }

    override fun onRowClick(row: DeepLinksItem.LinkRow) {
        startActivity(LinkBreakdownActivity.newIntent(requireContext(), row.link))
    }

    override fun onLaunchClick(
        row: DeepLinksItem.LinkRow,
        anchor: View,
    ) {
        val link = row.launchLink ?: return
        // Only web links ask, because App Links verification decides where they open. Any other link opens
        // the way a user's tap would. If another app or a saved "Always" choice takes it, the launch counts
        // as successful and Open in this app isn't offered: an accepted trade-off.
        if (LinkType.from(link.toUri().scheme) == LinkType.WEB) {
            LaunchModePopup.show(anchor) { mode -> launch(link, mode) }
        } else {
            launch(link, LaunchMode.REAL_TAP)
        }
    }

    override fun onMoreClick(
        row: DeepLinksItem.LinkRow,
        anchor: View,
    ) {
        PopupMenu(anchor.context, anchor).apply {
            menuInflater.inflate(R.menu.sentinel_deep_links_row_menu, menu)
            if (row is DeepLinksItem.BaseLinkRow) {
                menu.setGroupVisible(R.id.variantEditActions, false)
                menu.setGroupVisible(R.id.variantDeleteActions, false)
                // A declared template isn't a real link, so there is nothing to copy or share.
                if (row.launchLink == null) {
                    menu.findItem(R.id.copy)?.isVisible = false
                    menu.findItem(R.id.share)?.isVisible = false
                }
            }
            setOnMenuItemClickListener { onRowMenuClick(row, it.itemId) }
            show()
        }
    }

    override fun onAddVariantClick(row: DeepLinksItem.BaseLinkRow) {
        startActivity(
            VariantEditorActivity.newIntent(
                requireContext(),
                VariantEditorTarget.NewFromBase(
                    baseKey = row.baseLink.key,
                    componentName = (row.baseLink.origin as? BaseLinkOrigin.Declared)?.componentName,
                ),
            ),
        )
    }

    override fun onStatusClick(row: DeepLinksItem.LinkRow) {
        row.status?.let(::showStatus)
    }

    override fun onNoticeActionClick(notice: DeepLinksNotice) {
        if (notice == DeepLinksNotice.LINK_HANDLING_OFF) openLinkSettings()
    }

    private fun onRowMenuClick(
        row: DeepLinksItem.LinkRow,
        itemId: Int,
    ): Boolean =
        when (itemId) {
            R.id.breakdown -> {
                startActivity(LinkBreakdownActivity.newIntent(requireContext(), row.link))
                true
            }

            R.id.copy -> {
                requireContext().copyToClipboard(key = row.title ?: row.link, value = row.link)
                true
            }

            R.id.share -> {
                ShareCompat.IntentBuilder(requireActivity()).shareText(row.link)
                true
            }

            else -> (row as? DeepLinksItem.VariantRow)?.let { onVariantMenuClick(it, itemId) } ?: false
        }

    private fun onVariantMenuClick(
        row: DeepLinksItem.VariantRow,
        itemId: Int,
    ): Boolean {
        val focus =
            when (itemId) {
                R.id.edit -> VariantEditorFocus.NONE
                R.id.addParameter -> VariantEditorFocus.NEW_PARAMETER
                R.id.rename -> VariantEditorFocus.NAME
                else -> null
            }
        return when {
            focus != null -> {
                startActivity(
                    VariantEditorActivity.newIntent(requireContext(), VariantEditorTarget.Existing(row.variant.id), focus),
                )
                true
            }

            itemId == R.id.duplicate -> {
                viewModel.duplicate(row.variant.id, getString(R.string.sentinel_deep_links_copy_suffix))
                true
            }

            itemId == R.id.delete -> {
                confirmDeleteVariant(row)
                true
            }

            else -> false
        }
    }

    private fun confirmDeleteVariant(row: DeepLinksItem.VariantRow) {
        if (childFragmentManager.isStateSaved) return
        ConfirmationDialogFragment
            .newInstance(
                requestKey = REQUEST_DELETE_VARIANT,
                title = getString(R.string.sentinel_deep_links_delete_variant_title),
                message = row.link,
                positive = getString(R.string.sentinel_deep_links_delete),
                negative = getString(R.string.sentinel_deep_links_cancel),
                payload = row.variant.id,
            ).show(childFragmentManager, ConfirmationDialogFragment.TAG)
    }

    private fun confirmDeleteAll() {
        if (childFragmentManager.isStateSaved) return
        ConfirmationDialogFragment
            .newInstance(
                requestKey = REQUEST_DELETE_ALL,
                title = getString(R.string.sentinel_deep_links_delete_all_title),
                message = resources.getQuantityString(R.plurals.sentinel_deep_links_delete_all_message, variantCount, variantCount),
                positive = getString(R.string.sentinel_deep_links_delete),
                negative = getString(R.string.sentinel_deep_links_cancel),
            ).show(childFragmentManager, ConfirmationDialogFragment.TAG)
    }

    /** Shows the dialog that explains [status] (FR-42). */
    private fun showStatus(status: VerificationStatus) {
        if (childFragmentManager.isStateSaved) return
        VerificationStatusDialogFragment
            .newInstance(status)
            .show(childFragmentManager, VerificationStatusDialogFragment.TAG)
    }

    /**
     * Opens the system "Open by default" settings of this app, or its app details when that fails (FR-41, FR-44).
     * Below Android 12 there is nothing to open. Shows a Snackbar when neither screen could be opened.
     */
    private fun openLinkSettings() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        val packageName = requireContext().packageName
        val isOpened =
            startSettings(Intent(Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS, "package:$packageName".toUri())) ||
                startSettings(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null)))
        if (!isOpened) showSnackbar(getString(R.string.sentinel_deep_links_notice_link_settings_failed))
    }

    private fun startSettings(intent: Intent): Boolean =
        try {
            startActivity(intent)
            true
        } catch (ignored: RuntimeException) {
            // ActivityNotFoundException or a SecurityException from a restricted settings screen: try the next one.
            false
        }

    private fun setupDialogResults() {
        childFragmentManager.setFragmentResultListener(VerificationStatusDialogFragment.REQUEST_KEY, viewLifecycleOwner) { _, result ->
            if (result.getBoolean(VerificationStatusDialogFragment.RESULT_OPEN_LINK_SETTINGS)) openLinkSettings()
        }
        childFragmentManager.setFragmentResultListener(REQUEST_DELETE_VARIANT, viewLifecycleOwner) { _, result ->
            if (result.getBoolean(ConfirmationDialogFragment.RESULT_CONFIRMED)) {
                result.getString(ConfirmationDialogFragment.RESULT_PAYLOAD)?.let { viewModel.delete(it) }
            }
        }
        childFragmentManager.setFragmentResultListener(REQUEST_DELETE_ALL, viewLifecycleOwner) { _, result ->
            if (result.getBoolean(ConfirmationDialogFragment.RESULT_CONFIRMED)) {
                viewModel.deleteAll()
            }
        }
    }

    /** Launches [link] in [mode]. A [isRetry] launch comes from a Snackbar action and offers no further retry. */
    private fun launch(
        link: String,
        mode: LaunchMode,
        isRetry: Boolean = false,
    ) {
        showLaunchFeedback(link, DeepLinkLauncher(requireActivity()).launch(link, mode), isRetry)
    }

    private fun showLaunchFeedback(
        link: String,
        result: LaunchResult,
        isRetry: Boolean,
    ) {
        when (result) {
            LaunchResult.Launched -> Unit
            // A screen whose filter lacks BROWSABLE can't take a Real tap, but it can take the link directly.
            LaunchResult.NoHandler ->
                showSnackbar(
                    message = getString(R.string.sentinel_deep_links_no_handler),
                    actionLabel = getString(R.string.sentinel_deep_links_open_in_app).takeUnless { isRetry },
                    action = { launch(link, LaunchMode.OPEN_IN_APP, isRetry = true) },
                )
            LaunchResult.NotHandledByApp ->
                showSnackbar(
                    message = getString(R.string.sentinel_deep_links_not_handled),
                    actionLabel = getString(R.string.sentinel_deep_links_try_real_tap).takeUnless { isRetry },
                    action = { launch(link, LaunchMode.REAL_TAP, isRetry = true) },
                )
            is LaunchResult.Failed ->
                showSnackbar(
                    message = getString(R.string.sentinel_deep_links_launch_failed, result.reason),
                    maxLines = LAUNCH_FAILED_MAX_LINES,
                )
        }
    }

    private fun showSnackbar(
        message: String,
        actionLabel: String? = null,
        action: (() -> Unit)? = null,
        maxLines: Int? = null,
    ) {
        Snackbar
            .make(binding.root, message, Snackbar.LENGTH_LONG)
            .apply { if (actionLabel != null && action != null) setAction(actionLabel) { action() } }
            .apply { maxLines?.let { setTextMaxLines(it) } }
            .show()
    }

    private fun setupToolbar() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            binding.toolbar.updatePadding(left = bars.left, right = bars.right)
            insets
        }
        with(binding) {
            toolbar.setNavigationOnClickListener { requireActivity().finish() }
            toolbar.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.openLinkSettings -> {
                        openLinkSettings()
                        true
                    }

                    R.id.deleteAll -> {
                        confirmDeleteAll()
                        true
                    }

                    else -> false
                }
            }
        }
    }

    private fun setupRecyclerView() {
        adapter.stateRestorationPolicy = RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY
        with(binding.recyclerView) {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = this@DeepLinksFragment.adapter
            edgeEffectFactory = BounceEdgeEffectFactory()
            addItemDecoration(
                DeepLinksDividerDecoration(context) { position ->
                    this@DeepLinksFragment.adapter.currentList.getOrNull(position)
                },
            )
            ViewCompat.setOnApplyWindowInsetsListener(this) { target, insets ->
                val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
                target.updatePadding(left = bars.left, right = bars.right, bottom = bars.bottom)
                insets
            }
        }
    }

    private fun showOverview(overview: DeepLinksOverview) {
        val list = binding.recyclerView
        // A row's divider depends on the row above it, so recompute the dividers after every list update.
        adapter.submitList(overview.flatten()) { list.invalidateItemDecorations() }
        with(binding) {
            recyclerView.isGone = overview.isEmpty
            emptyStateLayout.isVisible = overview.isEmpty
        }
    }
}
