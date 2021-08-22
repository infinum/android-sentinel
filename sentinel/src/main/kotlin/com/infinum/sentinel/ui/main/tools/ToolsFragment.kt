package com.infinum.sentinel.ui.main.tools

import android.os.Bundle
import android.view.View
import androidx.annotation.RestrictTo
import androidx.core.content.ContextCompat
import com.infinum.sentinel.R
import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.databinding.SentinelFragmentToolsBinding
import com.infinum.sentinel.databinding.SentinelViewItemButtonBinding
import com.infinum.sentinel.ui.shared.base.BaseChildFragment
import com.infinum.sentinel.ui.shared.delegates.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class ToolsFragment : BaseChildFragment(R.layout.sentinel_fragment_tools) {

    companion object {
        fun newInstance() = ToolsFragment()
        val TAG: String = ToolsFragment::class.java.name
    }

    override val binding: SentinelFragmentToolsBinding by viewBinding(
        SentinelFragmentToolsBinding::bind
    )

    override val viewModel: ToolsViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.data(this@ToolsFragment::bind)
    }

    private fun bind(tools: Set<Sentinel.Tool>) =
        with(binding) {
            contentLayout.removeAllViews()
            tools.forEach {
                contentLayout.addView(createItemView(it))
            }
        }

    private fun createItemView(tool: Sentinel.Tool): View =
        SentinelViewItemButtonBinding.inflate(layoutInflater, binding.contentLayout, false)
            .apply {
                this.buttonView.icon =
                    tool.icon()?.let { ContextCompat.getDrawable(this.buttonView.context, it) }
                this.buttonView.text = getString(tool.name())
                this.buttonView.setOnClickListener(tool.listener())
            }.root
}
