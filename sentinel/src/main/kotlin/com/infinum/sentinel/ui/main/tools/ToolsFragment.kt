package com.infinum.sentinel.ui.main.tools

import android.view.View
import androidx.annotation.RestrictTo
import androidx.core.content.ContextCompat
import com.infinum.sentinel.R
import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.databinding.SentinelFragmentToolsBinding
import com.infinum.sentinel.databinding.SentinelViewItemButtonBinding
import com.infinum.sentinel.extensions.viewModels
import com.infinum.sentinel.ui.shared.base.BaseChildFragment
import com.infinum.sentinel.ui.shared.delegates.viewBinding

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class ToolsFragment : BaseChildFragment<ToolsState, Nothing>(R.layout.sentinel_fragment_tools) {

    companion object {
        fun newInstance() = ToolsFragment()
        const val TAG: String = "ToolsFragment"
    }

    override val binding: SentinelFragmentToolsBinding by viewBinding(
        SentinelFragmentToolsBinding::bind
    )

    override val viewModel: ToolsViewModel by viewModels()

    override fun onState(state: ToolsState) =
        when (state) {
            is ToolsState.Data -> with(binding) {
                contentLayout.removeAllViews()
                state.value.forEach {
                    contentLayout.addView(createItemView(it))
                }
            }
        }

    override fun onEvent(event: Nothing) = Unit

    private fun createItemView(tool: Sentinel.Tool): View =
        SentinelViewItemButtonBinding.inflate(layoutInflater, binding.contentLayout, false)
            .apply {
                this.buttonView.icon =
                    tool.icon()?.let { ContextCompat.getDrawable(this.buttonView.context, it) }
                this.buttonView.text = getString(tool.name())
                this.buttonView.setOnClickListener(tool.listener())
            }.root
}
