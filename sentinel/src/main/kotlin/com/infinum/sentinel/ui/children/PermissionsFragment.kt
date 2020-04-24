package com.infinum.sentinel.ui.children

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RestrictTo
import com.infinum.sentinel.R
import com.infinum.sentinel.data.sources.raw.PermissionsCollector
import com.infinum.sentinel.databinding.SentinelFragmentPermissionsBinding
import com.infinum.sentinel.databinding.SentinelViewItemCheckableBinding
import com.infinum.sentinel.ui.shared.BaseChildFragment
import org.koin.android.ext.android.get

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class PermissionsFragment : BaseChildFragment<SentinelFragmentPermissionsBinding>() {

    companion object {
        fun newInstance() = PermissionsFragment()
        val TAG: String = PermissionsFragment::class.java.simpleName
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): SentinelFragmentPermissionsBinding =
        SentinelFragmentPermissionsBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val collector: PermissionsCollector = get()
        collector.collect()
        collector.present().let {
            with(viewBinding) {
                contentLayout.removeAllViews()
                it.forEach {
                    contentLayout.addView(createItemView(it))
                }
            }
        }
    }

    private fun createItemView(entry: Map.Entry<String, Boolean>): View =
        SentinelViewItemCheckableBinding.inflate(layoutInflater, viewBinding.contentLayout, false)
            .apply {
                this.labelView.text = entry.key
                this.valueView.setImageResource(
                    if (entry.value) {
                        R.drawable.sentinel_ic_checked
                    } else {
                        R.drawable.sentinel_ic_unchecked
                    }
                )
            }.root
}
