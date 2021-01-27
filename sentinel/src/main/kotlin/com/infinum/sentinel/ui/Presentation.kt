package com.infinum.sentinel.ui

import android.content.Context
import android.content.Intent
import com.infinum.sentinel.BuildConfig
import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.domain.Domain
import com.infinum.sentinel.ui.application.ApplicationViewModel
import com.infinum.sentinel.ui.device.DeviceViewModel
import com.infinum.sentinel.ui.permissions.PermissionsViewModel
import com.infinum.sentinel.ui.preferences.PreferencesViewModel
import com.infinum.sentinel.ui.settings.SettingsViewModel
import com.infinum.sentinel.ui.tools.ToolsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.module.Module
import org.koin.dsl.module
import timber.log.Timber

internal object Presentation {

    init {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    private lateinit var context: Context

    fun init(context: Context) {
        this.context = context
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
        viewModel { SettingsViewModel(get(), get()) }
    }

    fun setup(tools: Set<Sentinel.Tool>, onTriggered: () -> Unit) {
        Domain.setup(tools, onTriggered)
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
