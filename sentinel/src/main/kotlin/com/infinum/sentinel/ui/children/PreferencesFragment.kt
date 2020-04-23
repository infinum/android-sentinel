package com.infinum.sentinel.ui.children

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RestrictTo
import com.infinum.sentinel.data.models.raw.PreferencesData
import com.infinum.sentinel.data.sources.raw.PreferencesCollector
import com.infinum.sentinel.databinding.SentinelFragmentPreferencesBinding
import com.infinum.sentinel.databinding.SentinelViewItemEditableBinding
import com.infinum.sentinel.databinding.SentinelViewItemPreferenceBinding
import com.infinum.sentinel.ui.shared.BaseChildFragment
import org.koin.android.ext.android.get

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class PreferencesFragment : BaseChildFragment<SentinelFragmentPreferencesBinding>() {

    companion object {
        fun newInstance() = PreferencesFragment()
        val TAG: String = PreferencesFragment::class.java.simpleName
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): SentinelFragmentPreferencesBinding =
        SentinelFragmentPreferencesBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val collector: PreferencesCollector = get()
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

    private fun createItemView(data: PreferencesData): View =
        SentinelViewItemPreferenceBinding.inflate(layoutInflater, viewBinding.contentLayout, false)
            .apply {
                nameView.text = data.name
                data.values.forEach {
                    prefsLayout.addView(
                        SentinelViewItemEditableBinding.inflate(layoutInflater, prefsLayout, false)
                            .apply {
                                labelView.text = it.second
                                valueView.setText(it.third.toString())
                            }.root
                    )
                }
            }.root
}
