package com.infinum.sentinel.ui.main.application

import androidx.annotation.RestrictTo
import com.infinum.sentinel.R
import com.infinum.sentinel.databinding.SentinelFragmentApplicationBinding
import com.infinum.sentinel.extensions.viewModels
import com.infinum.sentinel.ui.shared.base.BaseChildFragment
import com.infinum.sentinel.ui.shared.delegates.viewBinding

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class ApplicationFragment : BaseChildFragment<ApplicationState, Nothing>(R.layout.sentinel_fragment_application) {
    companion object {
        fun newInstance() = ApplicationFragment()

        const val TAG: String = "ApplicationFragment"
    }

    override val binding: SentinelFragmentApplicationBinding by viewBinding(
        SentinelFragmentApplicationBinding::bind,
    )

    override val viewModel: ApplicationViewModel by viewModels()

    override fun onState(state: ApplicationState) =
        when (state) {
            is ApplicationState.Data -> {
                with(binding) {
                    versionCodeView.data = state.value.versionCode
                    versionNameView.data = state.value.versionName ?: ""
                    firstInstallView.data = state.value.firstInstall
                    lastUpdateView.data = state.value.lastUpdate
                    minSdkView.data = state.value.minSdk
                    targetSdkView.data = state.value.targetSdk
                    packageNameView.data = state.value.packageName
                    processNameView.data = state.value.processName
                    taskAffinityView.data = state.value.taskAffinity
                    localeLanguageView.data = state.value.localeLanguage
                    localeCountryView.data = state.value.localeCountry
                    installerPackageView.data = state.value.installerPackageId
                        .takeIf { it.isNotBlank() } ?: getString(R.string.sentinel_unknown)
                }
            }
        }

    override fun onEvent(event: Nothing) = Unit
}
