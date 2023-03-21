package com.infinum.sentinel.ui

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.text.format.Formatter
import com.google.android.material.snackbar.Snackbar
import com.infinum.sentinel.R
import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.di.LibraryKoin
import com.infinum.sentinel.domain.Domain
import com.infinum.sentinel.domain.Repositories
import com.infinum.sentinel.domain.bundle.descriptor.models.BundleDescriptor
import com.infinum.sentinel.domain.bundle.descriptor.models.BundleParameters
import com.infinum.sentinel.domain.bundle.monitor.models.BundleMonitorParameters
import com.infinum.sentinel.domain.certificate.monitor.models.CertificateMonitorParameters
import com.infinum.sentinel.domain.crash.monitor.models.CrashMonitorParameters
import com.infinum.sentinel.domain.triggers.models.TriggerParameters
import com.infinum.sentinel.extensions.sizeTree
import com.infinum.sentinel.ui.Presentation.Constants.BYTE_MULTIPLIER
import com.infinum.sentinel.ui.bundles.BundlesViewModel
import com.infinum.sentinel.ui.bundles.callbacks.BundleMonitorActivityCallbacks
import com.infinum.sentinel.ui.bundles.callbacks.BundleMonitorNotificationCallbacks
import com.infinum.sentinel.ui.bundles.details.BundleDetailsActivity
import com.infinum.sentinel.ui.bundles.details.BundleDetailsViewModel
import com.infinum.sentinel.ui.certificates.CertificatesViewModel
import com.infinum.sentinel.ui.certificates.details.CertificateDetailsViewModel
import com.infinum.sentinel.ui.certificates.observer.CertificatesObserver
import com.infinum.sentinel.ui.certificates.observer.SentinelWorkManager
import com.infinum.sentinel.ui.crash.CrashesViewModel
import com.infinum.sentinel.ui.crash.anr.SentinelAnrObserver
import com.infinum.sentinel.ui.crash.anr.SentinelAnrObserverRunnable
import com.infinum.sentinel.ui.crash.anr.SentinelUiAnrObserver
import com.infinum.sentinel.ui.crash.details.CrashDetailsViewModel
import com.infinum.sentinel.ui.crash.handler.SentinelExceptionHandler
import com.infinum.sentinel.ui.crash.handler.SentinelUncaughtExceptionHandler
import com.infinum.sentinel.ui.main.SentinelActivity
import com.infinum.sentinel.ui.main.SentinelViewModel
import com.infinum.sentinel.ui.main.application.ApplicationViewModel
import com.infinum.sentinel.ui.main.device.DeviceViewModel
import com.infinum.sentinel.ui.main.permissions.PermissionsViewModel
import com.infinum.sentinel.ui.main.preferences.PreferencesViewModel
import com.infinum.sentinel.ui.main.preferences.editor.PreferenceEditorViewModel
import com.infinum.sentinel.ui.main.tools.ToolsViewModel
import com.infinum.sentinel.ui.settings.SettingsViewModel
import com.infinum.sentinel.ui.shared.notification.IntentFactory
import com.infinum.sentinel.ui.shared.notification.NotificationFactory
import com.infinum.sentinel.ui.shared.notification.NotificationIntentFactory
import com.infinum.sentinel.ui.shared.notification.SystemNotificationFactory
import com.infinum.sentinel.ui.tools.AppInfoTool
import com.infinum.sentinel.ui.tools.BundleMonitorTool
import com.infinum.sentinel.ui.tools.CertificateTool
import com.infinum.sentinel.ui.tools.CrashMonitorTool
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module

@SuppressLint("StaticFieldLeak")
@Suppress("TooManyFunctions")
internal object Presentation {

    object Constants {
        const val BYTE_MULTIPLIER = 1000
        const val SHARE_MIME_TYPE = "text/plain"
        const val NOTIFICATIONS_CHANNEL_ID = "sentinel"

        object Keys {
            const val BUNDLE_ID = "KEY_BUNDLE_ID"
            const val SHOULD_REFRESH: String = "KEY_SHOULD_REFRESH"
            const val APPLICATION_NAME: String = "KEY_APPLICATION_NAME"
            const val CRASH_ID: String = "KEY_CRASH_ID"
            const val NOTIFY_INVALID_NOW: String = "KEY_NOTIFY_INVALID_NOW"
            const val NOTIFY_TO_EXPIRE: String = "KEY_NOTIFY_TO_EXPIRE"
            const val EXPIRE_IN_AMOUNT: String = "KEY_EXPIRE_IN_AMOUNT"
            const val EXPIRE_IN_UNIT: String = "KEY_EXPIRE_IN_UNIT"
        }
    }

    private val DEFAULT_TOOLS = setOf(
        CrashMonitorTool(),
        BundleMonitorTool(),
        AppInfoTool()
    )

    private lateinit var context: Context

    private val scope = MainScope()

    fun initialize(context: Context) {
        this.context = context

        initializeCrashMonitor()
        initializeBundleMonitor()
    }

    fun setExceptionHandler(handler: Thread.UncaughtExceptionHandler?) {
        val exceptionHandler = LibraryKoin.koin().get<SentinelExceptionHandler>()
        exceptionHandler.setExceptionHandler(handler)
    }

    fun setAnrListener(listener: Sentinel.ApplicationNotRespondingListener?) {
        val observer = LibraryKoin.koin().get<SentinelAnrObserver>()
        observer.setListener(listener)
    }

    fun setup(tools: Set<Sentinel.Tool>, onTriggered: () -> Unit) {
        Domain.setup(
            tools.plus(DEFAULT_TOOLS),
            tools.filterIsInstance<CertificateTool>().firstOrNull()?.userCertificates.orEmpty(),
            onTriggered
        )
        initializeTriggers()
        initializeCertificateMonitor()
    }

    fun show() =
        if (this::context.isInitialized) {
            context.startActivity(
                Intent(context, SentinelActivity::class.java)
                    .apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    }
            )
        } else {
            throw NullPointerException("Presentation context has not been initialized.")
        }

    fun modules(): List<Module> =
        Domain.modules().plus(
            listOf(
                viewModels(),
                factories()
            )
        )

    private fun initializeCrashMonitor() {
        val exceptionHandler = LibraryKoin.koin().get<SentinelExceptionHandler>()
        Thread.setDefaultUncaughtExceptionHandler(exceptionHandler as Thread.UncaughtExceptionHandler)
        val anrObserver = LibraryKoin.koin().get<SentinelAnrObserver>()

        val crashMonitor = LibraryKoin.koin().get<Repositories.CrashMonitor>()
        scope.launch {
            val entity = crashMonitor.load(CrashMonitorParameters()).first()
            if (entity.notifyExceptions) {
                exceptionHandler.start()
            } else {
                exceptionHandler.stop()
            }
            if (entity.notifyExceptions) {
                anrObserver.start()
            } else {
                anrObserver.stop()
            }
        }
    }

    private fun initializeBundleMonitor() {
        val bundleMonitor = LibraryKoin.koin().get<Repositories.BundleMonitor>()
        val bundles = LibraryKoin.koin().get<Repositories.Bundles>()
        val notificationCallbacks = BundleMonitorNotificationCallbacks()

        (this.context.applicationContext as? Application)?.registerActivityLifecycleCallbacks(notificationCallbacks)
        (this.context.applicationContext as? Application)
            ?.registerActivityLifecycleCallbacks(
                BundleMonitorActivityCallbacks { activity, timestamp, className, callSite, bundle ->
                    scope.launch {
                        val sizeTree = bundle.sizeTree()
                        bundles.save(
                            BundleParameters(
                                descriptor = BundleDescriptor(
                                    timestamp = timestamp,
                                    className = className,
                                    callSite = callSite,
                                    bundleTree = sizeTree
                                )
                            )
                        )

                        val currentMonitor = bundleMonitor.load(BundleMonitorParameters()).first()
                        if (currentMonitor.notify && sizeTree.size > currentMonitor.limit * BYTE_MULTIPLIER) {
                            notificationCallbacks.currentActivity?.let {
                                Snackbar.make(
                                    it.window.decorView,
                                    "$className${System.lineSeparator()}" +
                                        Formatter.formatFileSize(activity, sizeTree.size.toLong()),
                                    Snackbar.LENGTH_LONG
                                )
                                    .setAction(R.string.sentinel_show) { view ->
                                        view.context.startActivity(
                                            Intent(activity, BundleDetailsActivity::class.java)
                                                .apply {
                                                    putExtra(Constants.Keys.BUNDLE_ID, sizeTree.id)
                                                }
                                        )
                                    }
                                    .show()
                            }
                        }
                    }
                }
            )
    }

    private fun initializeTriggers() {
        val triggers = LibraryKoin.koin().get<Repositories.Triggers>()
        scope.launch {
            triggers.load(TriggerParameters()).first()
        }
    }

    private fun initializeCertificateMonitor() {
        val certificateMonitor = LibraryKoin.koin().get<Repositories.CertificateMonitor>()
        val certificatesObserver = LibraryKoin.koin().get<CertificatesObserver>()
        val workManager = LibraryKoin.koin().get<SentinelWorkManager>()
        scope.launch {
            val monitorEntity = certificateMonitor.load(CertificateMonitorParameters()).first()
            monitorEntity.takeIf { it.runOnStart }?.let {
                certificatesObserver.activate(it)
            } ?: certificatesObserver.deactivate()
            monitorEntity.takeIf { it.runInBackground }?.let {
                workManager.startCertificatesCheck(it)
            } ?: workManager.stopCertificatesCheck()
        }
    }

    private fun viewModels() = module {
        viewModel { SentinelViewModel(get(), get(), get(), get()) }
        viewModel { DeviceViewModel(get()) }
        viewModel { ApplicationViewModel(get()) }
        viewModel { PermissionsViewModel(get()) }
        viewModel { PreferencesViewModel(get(), get()) }
        viewModel { PreferenceEditorViewModel(get()) }
        viewModel { ToolsViewModel(get()) }
        viewModel { SettingsViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
        viewModel { BundlesViewModel(get(), get()) }
        viewModel { BundleDetailsViewModel(get()) }
        viewModel { CrashesViewModel(get()) }
        viewModel { CrashDetailsViewModel(get(), get(), get(), get()) }
        viewModel { CertificatesViewModel(get(), get(), get()) }
        viewModel { CertificateDetailsViewModel(get(), get()) }
    }

    private fun factories() = module {
        factory<IntentFactory> { NotificationIntentFactory(get()) }
        factory<NotificationFactory> { SystemNotificationFactory(get(), get()) }

        single<SentinelExceptionHandler> { SentinelUncaughtExceptionHandler(get(), get(), get()) }

        single { SentinelAnrObserverRunnable(get(), get(), get()) }
        factory<ExecutorService> { Executors.newSingleThreadExecutor() }
        single<SentinelAnrObserver> { SentinelUiAnrObserver(get(), get()) }

        single { CertificatesObserver(get(), get(), get()) }
        single { SentinelWorkManager(get()) }
    }
}
