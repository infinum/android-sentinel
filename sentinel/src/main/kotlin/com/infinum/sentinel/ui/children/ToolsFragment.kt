package com.infinum.sentinel.ui.children

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RestrictTo
import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.databinding.SentinelFragmentToolsBinding
import com.infinum.sentinel.databinding.SentinelViewItemButtonBinding
import com.infinum.sentinel.domain.repository.CollectorRepository
import com.infinum.sentinel.ui.shared.BaseChildFragment

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class ToolsFragment : BaseChildFragment<SentinelFragmentToolsBinding>() {

    companion object {
        fun newInstance() = ToolsFragment()
        val TAG: String = ToolsFragment::class.java.simpleName
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): SentinelFragmentToolsBinding =
        SentinelFragmentToolsBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(CollectorRepository.tools()) {
            collect()
            present().let {
                with(viewBinding) {
                    contentLayout.removeAllViews()
                    it.forEach {
                        contentLayout.addView(createItemView(it))
                    }
                }
            }
        }
    }

    private fun createItemView(tool: Sentinel.Tool): View =
        SentinelViewItemButtonBinding.inflate(layoutInflater, viewBinding.contentLayout, false)
            .apply {
                this.buttonView.text = getString(tool.name())
                this.buttonView.setOnClickListener(tool.listener())
            }.root
}
