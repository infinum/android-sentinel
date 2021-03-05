package com.infinum.sentinel.ui.main.preferences

import android.os.Bundle
import android.view.View
import androidx.annotation.RestrictTo
import com.infinum.sentinel.R
import com.infinum.sentinel.data.models.raw.PreferencesData
import com.infinum.sentinel.databinding.SentinelFragmentPreferencesBinding
import com.infinum.sentinel.databinding.SentinelViewItemPreferenceBinding
import com.infinum.sentinel.databinding.SentinelViewItemTextBinding
import com.infinum.sentinel.ui.shared.base.BaseChildFragment
import com.infinum.sentinel.ui.shared.delegates.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class PreferencesFragment : BaseChildFragment(R.layout.sentinel_fragment_preferences) {

    companion object {
        fun newInstance() = PreferencesFragment()
        val TAG: String = PreferencesFragment::class.java.name
    }

    override val binding: SentinelFragmentPreferencesBinding by viewBinding(
        SentinelFragmentPreferencesBinding::bind
    )

    override val viewModel: PreferencesViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.data(this@PreferencesFragment::bind)
    }

    private fun bind(preferences: List<PreferencesData>) =
        with(binding) {
            contentLayout.removeAllViews()
            preferences.forEach {
                contentLayout.addView(createItemView(it))
            }
        }

    private fun createItemView(data: PreferencesData): View =
        SentinelViewItemPreferenceBinding.inflate(layoutInflater, binding.contentLayout, false)
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
