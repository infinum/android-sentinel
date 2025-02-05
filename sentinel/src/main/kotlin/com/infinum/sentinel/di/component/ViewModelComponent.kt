package com.infinum.sentinel.di.component

import androidx.lifecycle.ViewModel
import com.infinum.sentinel.ui.bundles.BundlesViewModel
import com.infinum.sentinel.ui.bundles.details.BundleDetailsViewModel
import com.infinum.sentinel.ui.certificates.CertificatesViewModel
import com.infinum.sentinel.ui.certificates.details.CertificateDetailsViewModel
import com.infinum.sentinel.ui.crash.CrashesViewModel
import com.infinum.sentinel.ui.crash.details.CrashDetailsViewModel
import com.infinum.sentinel.ui.main.SentinelViewModel
import com.infinum.sentinel.ui.main.application.ApplicationViewModel
import com.infinum.sentinel.ui.main.device.DeviceViewModel
import com.infinum.sentinel.ui.main.permissions.PermissionsViewModel
import com.infinum.sentinel.ui.main.preferences.all.AllPreferencesViewModel
import com.infinum.sentinel.ui.main.preferences.targeted.TargetedPreferencesViewModel
import com.infinum.sentinel.ui.main.preferences.editor.PreferenceEditorViewModel
import com.infinum.sentinel.ui.main.tools.ToolsViewModel
import com.infinum.sentinel.ui.settings.SettingsViewModel
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.IntoMap
import me.tatarka.inject.annotations.Provides

@Suppress("TooManyFunctions")
@Component
internal abstract class ViewModelComponent(
    @Component val domainComponent: DomainComponent
) {

    abstract val viewModelMap: Map<Class<*>, ViewModel>

    abstract val sentinel: SentinelViewModel

    abstract val device: DeviceViewModel

    abstract val application: ApplicationViewModel

    abstract val permissions: PermissionsViewModel

    abstract val allPreferences: AllPreferencesViewModel

    abstract val targetedPreferences: TargetedPreferencesViewModel

    abstract val preferenceEditor: PreferenceEditorViewModel

    abstract val tools: ToolsViewModel

    abstract val settings: SettingsViewModel

    abstract val bundles: BundlesViewModel

    abstract val bundleDetails: BundleDetailsViewModel

    abstract val crashes: CrashesViewModel

    abstract val crashDetails: CrashDetailsViewModel

    abstract val certificates: CertificatesViewModel

    abstract val certificateDetails: CertificateDetailsViewModel

    @IntoMap
    @Provides
    fun sentinel(): Pair<Class<*>, ViewModel> =
        SentinelViewModel::class.java to sentinel

    @IntoMap
    @Provides
    fun device(): Pair<Class<*>, ViewModel> =
        DeviceViewModel::class.java to device

    @IntoMap
    @Provides
    fun application(): Pair<Class<*>, ViewModel> =
        ApplicationViewModel::class.java to application

    @IntoMap
    @Provides
    fun permissions(): Pair<Class<*>, ViewModel> =
        PermissionsViewModel::class.java to permissions

    @IntoMap
    @Provides
    fun allPreferences(): Pair<Class<*>, ViewModel> =
        AllPreferencesViewModel::class.java to allPreferences

    @IntoMap
    @Provides
    fun targetedPreferences(): Pair<Class<*>, ViewModel> =
        TargetedPreferencesViewModel::class.java to targetedPreferences

    @IntoMap
    @Provides
    fun preferenceEditor(): Pair<Class<*>, ViewModel> =
        PreferenceEditorViewModel::class.java to preferenceEditor

    @IntoMap
    @Provides
    fun tools(): Pair<Class<*>, ViewModel> =
        ToolsViewModel::class.java to tools

    @IntoMap
    @Provides
    fun settings(): Pair<Class<*>, ViewModel> =
        SettingsViewModel::class.java to settings

    @IntoMap
    @Provides
    fun bundles(): Pair<Class<*>, ViewModel> =
        BundlesViewModel::class.java to bundles

    @IntoMap
    @Provides
    fun bundleDetails(): Pair<Class<*>, ViewModel> =
        BundleDetailsViewModel::class.java to bundleDetails

    @IntoMap
    @Provides
    fun crashes(): Pair<Class<*>, ViewModel> =
        CrashesViewModel::class.java to crashes

    @IntoMap
    @Provides
    fun crashDetails(): Pair<Class<*>, ViewModel> =
        CrashDetailsViewModel::class.java to crashDetails

    @IntoMap
    @Provides
    fun certificates(): Pair<Class<*>, ViewModel> =
        CertificatesViewModel::class.java to certificates

    @IntoMap
    @Provides
    fun certificateDetails(): Pair<Class<*>, ViewModel> =
        CertificateDetailsViewModel::class.java to certificateDetails
}
