package com.infinum.sentinel.ui

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.text.format.Formatter
import com.google.android.material.snackbar.Snackbar
import com.infinum.sentinel.BuildConfig
import com.infinum.sentinel.R
import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.data.models.memory.triggers.shake.ShakeTrigger
import com.infinum.sentinel.di.LibraryKoin
import com.infinum.sentinel.domain.Domain
import com.infinum.sentinel.domain.Repositories
import com.infinum.sentinel.domain.bundle.descriptor.models.BundleDescriptor
import com.infinum.sentinel.domain.bundle.descriptor.models.BundleParameters
import com.infinum.sentinel.domain.bundle.monitor.models.BundleMonitorParameters
import com.infinum.sentinel.extensions.sizeTree
import com.infinum.sentinel.ui.Presentation.Constants.BYTE_MULTIPLIER
import com.infinum.sentinel.ui.bundles.BundlesViewModel
import com.infinum.sentinel.ui.bundles.callbacks.BundleMonitorActivityCallbacks
import com.infinum.sentinel.ui.bundles.callbacks.BundleMonitorNotificationCallbacks
import com.infinum.sentinel.ui.bundles.details.BundleDetailsActivity
import com.infinum.sentinel.ui.bundles.details.BundleDetailsViewModel
import com.infinum.sentinel.ui.crash.CrashesViewModel
import com.infinum.sentinel.ui.crash.notification.NotificationFactory
import com.infinum.sentinel.ui.crash.handler.SentinelExceptionHandler
import com.infinum.sentinel.ui.crash.notification.SystemNotificationFactory
import com.infinum.sentinel.ui.crash.details.CrashDetailsViewModel
import com.infinum.sentinel.ui.main.SentinelActivity
import com.infinum.sentinel.ui.main.SentinelViewModel
import com.infinum.sentinel.ui.main.application.ApplicationViewModel
import com.infinum.sentinel.ui.main.device.DeviceViewModel
import com.infinum.sentinel.ui.main.permissions.PermissionsViewModel
import com.infinum.sentinel.ui.main.preferences.PreferencesViewModel
import com.infinum.sentinel.ui.main.preferences.editor.PreferenceEditorViewModel
import com.infinum.sentinel.ui.main.tools.ToolsViewModel
import com.infinum.sentinel.ui.settings.SettingsViewModel
import com.infinum.sentinel.ui.tools.AppInfoTool
import com.infinum.sentinel.ui.tools.BundleMonitorTool
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module
import timber.log.Timber

@SuppressLint("StaticFieldLeak")
internal object Presentation {

    object Constants {
        const val BYTE_MULTIPLIER = 1000

        object Keys {
            const val BUNDLE_ID = "KEY_BUNDLE_ID"
            const val SHOULD_REFRESH: String = "KEY_SHOULD_REFRESH"
            const val APPLICATION_NAME: String = "KEY_APPLICATION_NAME"
            const val CRASH_ID: String = "KEY_CRASH_ID"
        }
    }

    private val DEFAULT_TOOLS = setOf(
        BundleMonitorTool(),
        AppInfoTool()
    )

    private lateinit var context: Context

    init {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    fun initialize(context: Context) {
        Thread.setDefaultUncaughtExceptionHandler(
            LibraryKoin.koin().get()
        )

        this.context = context

        val bundleMonitor = LibraryKoin.koin().get<Repositories.BundleMonitor>()
        val bundles = LibraryKoin.koin().get<Repositories.Bundles>()

        val notificationCallbacks = BundleMonitorNotificationCallbacks()

        (this.context.applicationContext as? Application)?.registerActivityLifecycleCallbacks(notificationCallbacks)
        (this.context.applicationContext as? Application)
            ?.registerActivityLifecycleCallbacks(
                BundleMonitorActivityCallbacks { activity, timestamp, className, callSite, bundle ->
                    GlobalScope.launch {
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

    fun modules(): List<Module> =
        Domain.modules().plus(
            listOf(
                viewModels(),
                factories()
            )
        )

    fun setup(tools: Set<Sentinel.Tool>, onTriggered: () -> Unit) {
        Domain.setup(tools.plus(DEFAULT_TOOLS), onTriggered)
        LibraryKoin.koin().get<ShakeTrigger>().apply { active = true }
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

    private fun viewModels() = module {
        viewModel { SentinelViewModel(get(), get(), get(), get()) }
        viewModel { DeviceViewModel(get()) }
        viewModel { ApplicationViewModel(get()) }
        viewModel { PermissionsViewModel(get()) }
        viewModel { PreferencesViewModel(get(), get()) }
        viewModel { PreferenceEditorViewModel(get()) }
        viewModel { ToolsViewModel(get()) }
        viewModel { SettingsViewModel(get(), get(), get()) }
        viewModel { BundlesViewModel(get(), get()) }
        viewModel { BundleDetailsViewModel(get()) }
        viewModel { CrashesViewModel(get()) }
        viewModel { CrashDetailsViewModel(get()) }
    }

    private fun factories() = module {
        single<NotificationFactory> { SystemNotificationFactory(get()) }

        single<Thread.UncaughtExceptionHandler> { SentinelExceptionHandler(get(), get(), get()) }
    }
}
