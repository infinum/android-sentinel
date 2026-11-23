package com.infinum.sentinel.ui.deeplinks.breakdown

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RestrictTo
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isVisible
import androidx.core.view.updatePadding
import com.infinum.sentinel.R
import com.infinum.sentinel.databinding.SentinelFragmentLinkBreakdownBinding
import com.infinum.sentinel.databinding.SentinelViewItemTextBinding
import com.infinum.sentinel.extensions.copyToClipboard
import com.infinum.sentinel.extensions.viewModels
import com.infinum.sentinel.ui.shared.Constants
import com.infinum.sentinel.ui.shared.base.BaseChildFragment
import com.infinum.sentinel.ui.shared.delegates.viewBinding

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class LinkBreakdownFragment : BaseChildFragment<LinkBreakdownState, Nothing>(R.layout.sentinel_fragment_link_breakdown) {
    companion object {
        fun newInstance(link: String) =
            LinkBreakdownFragment().apply {
                arguments =
                    Bundle().apply {
                        putString(Constants.Keys.DEEP_LINK, link)
                    }
            }

        const val TAG: String = "LinkBreakdownFragment"
    }

    override val binding: SentinelFragmentLinkBreakdownBinding by viewBinding(
        SentinelFragmentLinkBreakdownBinding::bind,
    )

    override val viewModel: LinkBreakdownViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.setLink(arguments?.getString(Constants.Keys.DEEP_LINK).orEmpty())
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        binding.toolbar.setNavigationOnClickListener {
            requireActivity().finish()
        }
        setupInsets()
    }

    private fun setupInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            binding.toolbar.updatePadding(left = bars.left, right = bars.right)
            binding.scrollView.updatePadding(left = bars.left, right = bars.right, bottom = bars.bottom)
            insets
        }
    }

    override fun onState(state: LinkBreakdownState) =
        when (state) {
            is LinkBreakdownState.Data -> showBreakdown(state.breakdown)
        }

    override fun onEvent(event: Nothing) = Unit

    private fun showBreakdown(breakdown: LinkBreakdown) {
        binding.fullLinkView.text = breakdown.fullLink
        listOf(binding.partsContainer, binding.parametersContainer, binding.fragmentContainer).forEach {
            it.removeAllViews()
        }
        binding.unparseableView.isVisible = breakdown is LinkBreakdown.Unparseable
        binding.parametersHeaderView.isVisible = false
        if (breakdown is LinkBreakdown.Parsed) {
            showParts(breakdown)
        }
    }

    private fun showParts(breakdown: LinkBreakdown.Parsed) {
        with(binding.partsContainer) {
            addRow(this, getString(R.string.sentinel_deep_links_breakdown_scheme), breakdown.scheme)
            breakdown.host?.let { addRow(this, getString(R.string.sentinel_deep_links_breakdown_host), it) }
            breakdown.port?.let { addRow(this, getString(R.string.sentinel_deep_links_breakdown_port), it.toString()) }
            addRow(this, getString(R.string.sentinel_deep_links_breakdown_path), breakdown.path)
        }
        binding.parametersHeaderView.isVisible = breakdown.params.isNotEmpty()
        breakdown.params.forEach { addRow(binding.parametersContainer, it.key, it.value, isLabelVerbatim = true) }
        breakdown.fragment?.let {
            addRow(binding.fragmentContainer, getString(R.string.sentinel_deep_links_breakdown_fragment), it)
        }
    }

    /**
     * Adds a label and value row. The shared row layout draws labels in capitals; an [isLabelVerbatim] label,
     * such as a query key, is shown exactly as written because its case matters.
     */
    private fun addRow(
        container: ViewGroup,
        label: String,
        value: String,
        isLabelVerbatim: Boolean = false,
    ) {
        val row = SentinelViewItemTextBinding.inflate(LayoutInflater.from(container.context), container, false)
        row.labelView.text = label
        if (isLabelVerbatim) row.labelView.setAllCaps(false)
        row.valueView.text = value.ifEmpty { getString(R.string.sentinel_deep_links_breakdown_empty) }
        row.valueView.textDirection = View.TEXT_DIRECTION_LTR
        // Copies the real value, so an empty value is copied as an empty string and not as the placeholder.
        row.root.setOnLongClickListener {
            it.context.copyToClipboard(key = label, value = value)
        }
        container.addView(row.root)
    }
}
