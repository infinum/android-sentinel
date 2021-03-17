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
import com.infinum.sentinel.ui.main.SentinelActivity
import com.infinum.sentinel.ui.main.SentinelViewModel
import com.infinum.sentinel.ui.main.application.ApplicationViewModel
import com.infinum.sentinel.ui.main.device.DeviceViewModel
import com.infinum.sentinel.ui.main.permissions.PermissionsViewModel
import com.infinum.sentinel.ui.main.preferences.PreferencesViewModel
import com.infinum.sentinel.ui.main.tools.ToolsViewModel
import com.infinum.sentinel.ui.settings.SettingsViewModel
import com.infinum.sentinel.ui.tools.AppInfoTool
import com.infinum.sentinel.ui.tools.BundleMonitorTool
import kotlinx.coroutines.Dispatchers
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
        const val KEY_BUNDLE_ID = "KEY_BUNDLE_ID"
        const val BYTE_MULTIPLIER = 1000
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
        this.context = context

        val bundleMonitor = LibraryKoin.koin().get<Repositories.BundleMonitor>()
        val bundles = LibraryKoin.koin().get<Repositories.Bundles>()

        val notificationCallbacks = BundleMonitorNotificationCallbacks()

        (this.context.applicationContext as? Application)?.registerActivityLifecycleCallbacks(notificationCallbacks)
        (this.context.applicationContext as? Application)
            ?.registerActivityLifecycleCallbacks(
                BundleMonitorActivityCallbacks { activity, timestamp, className, callSite, bundle ->
                    val sizeTree = bundle.sizeTree()
                    GlobalScope.launch(Dispatchers.IO) {
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
                    }
                    GlobalScope.launch(Dispatchers.Main) {
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
                                                    putExtra(Constants.KEY_BUNDLE_ID, sizeTree.id)
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
                viewModels()
            )
        )

    private fun viewModels() = module {
        viewModel { SentinelViewModel(get(), get(), get(), get()) }
        viewModel { DeviceViewModel(get()) }
        viewModel { ApplicationViewModel(get()) }
        viewModel { PermissionsViewModel(get()) }
        viewModel { PreferencesViewModel(get()) }
        viewModel { ToolsViewModel(get()) }
        viewModel { SettingsViewModel(get(), get(), get()) }
        viewModel { BundlesViewModel(get(), get()) }
        viewModel { BundleDetailsViewModel(get()) }
    }

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
}
