package com.infinum.sentinel.ui.children

import android.os.Bundle
import android.view.View
import androidx.annotation.RestrictTo
import com.infinum.sentinel.R
import com.infinum.sentinel.databinding.SentinelFragmentApplicationBinding
import com.infinum.sentinel.ui.DependencyGraph
import com.infinum.sentinel.ui.shared.BaseChildFragment
import com.infinum.sentinel.ui.shared.viewBinding

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class ApplicationFragment : BaseChildFragment(R.layout.sentinel_fragment_application) {

    companion object {
        fun newInstance() = ApplicationFragment()
        val TAG: String = ApplicationFragment::class.java.simpleName
    }

    override val binding: SentinelFragmentApplicationBinding by viewBinding(
        SentinelFragmentApplicationBinding::bind
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        DependencyGraph.collectors().application().let {
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
