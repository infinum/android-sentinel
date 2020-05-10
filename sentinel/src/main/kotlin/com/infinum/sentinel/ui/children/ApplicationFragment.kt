package com.infinum.sentinel.ui.children

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RestrictTo
import com.infinum.sentinel.databinding.SentinelFragmentApplicationBinding
import com.infinum.sentinel.ui.DependencyGraph
import com.infinum.sentinel.ui.shared.BaseChildFragment

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class ApplicationFragment : BaseChildFragment<SentinelFragmentApplicationBinding>() {

    companion object {
        fun newInstance() = ApplicationFragment()
        val TAG: String = ApplicationFragment::class.java.simpleName
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): SentinelFragmentApplicationBinding =
        SentinelFragmentApplicationBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(DependencyGraph.collectors().application()) {
            collect()
            present().let {
                with(viewBinding) {
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
}
