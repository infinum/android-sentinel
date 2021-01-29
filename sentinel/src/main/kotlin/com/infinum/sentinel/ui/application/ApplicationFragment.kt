package com.infinum.sentinel.ui.application

import android.os.Bundle
import android.view.View
import androidx.annotation.RestrictTo
import com.infinum.sentinel.R
import com.infinum.sentinel.databinding.SentinelFragmentApplicationBinding
import com.infinum.sentinel.ui.shared.base.BaseChildFragment
import com.infinum.sentinel.ui.shared.delegates.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class ApplicationFragment : BaseChildFragment(R.layout.sentinel_fragment_application) {

    companion object {
        fun newInstance() = ApplicationFragment()
        val TAG: String = ApplicationFragment::class.java.name
    }

    override val binding: SentinelFragmentApplicationBinding by viewBinding(
        SentinelFragmentApplicationBinding::bind
    )

    override val viewModel: ApplicationViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.data {
            with(binding) {
                versionCodeView.data = it.versionCode
                versionNameView.data = it.versionName
                firstInstallView.data = it.firstInstall
                lastUpdateView.data = it.lastUpdate
                minSdkView.data = it.minSdk
                targetSdkView.data = it.targetSdk
                packageNameView.data = it.packageName
                processNameView.data = it.processName
                taskAffinityView.data = it.taskAffinity
                localeLanguageView.data = it.localeLanguage
                localeCountryView.data = it.localeCountry
            }
        }
    }
}
