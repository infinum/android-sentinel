package com.infinum.sentinel.ui.children

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RestrictTo
import com.infinum.sentinel.data.models.raw.PreferencesData
import com.infinum.sentinel.databinding.SentinelFragmentPreferencesBinding
import com.infinum.sentinel.databinding.SentinelViewItemPreferenceBinding
import com.infinum.sentinel.databinding.SentinelViewItemTextBinding
import com.infinum.sentinel.domain.repository.CollectorRepository
import com.infinum.sentinel.ui.shared.BaseChildFragment

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

        with(CollectorRepository.preferences()) {
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

    private fun createItemView(data: PreferencesData): View =
        SentinelViewItemPreferenceBinding.inflate(layoutInflater, viewBinding.contentLayout, false)
            .apply {
                nameView.text = data.name
                data.values.forEach {
                    prefsLayout.addView(
                        SentinelViewItemTextBinding.inflate(layoutInflater, prefsLayout, false)
                            .apply {
                                labelView.isAllCaps = false
                                labelView.text = it.second
                                valueView.text = it.third.toString()
                            }.root
                    )
                }
            }.root
}
