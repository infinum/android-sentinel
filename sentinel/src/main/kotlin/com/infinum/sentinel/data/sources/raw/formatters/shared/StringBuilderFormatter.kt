package com.infinum.sentinel.data.sources.raw.formatters.shared

import androidx.annotation.StringRes
import com.infinum.sentinel.R
import com.infinum.sentinel.data.models.raw.ApplicationData
import com.infinum.sentinel.data.models.raw.DeviceData

internal abstract class StringBuilderFormatter {

    abstract fun addLine(builder: StringBuilder, @StringRes tag: Int, text: String)

    abstract fun addLine(builder: StringBuilder, tag: String, text: String)

    internal fun addAllData(
        builder: StringBuilder,
        applicationData: String,
        permissionsData: String,
        deviceData: String,
        preferencesData: String
    ): String =
        builder
            .appendLine(applicationData)
            .appendLine(permissionsData)
            .appendLine(deviceData)
            .appendLine(preferencesData)
            .toString()

    internal fun addApplicationData(builder: StringBuilder, data: ApplicationData) {
        addLine(builder, R.string.sentinel_version_code, data.versionCode)
        addLine(builder, R.string.sentinel_version_name, data.versionName)
        addLine(builder, R.string.sentinel_first_install, data.firstInstall)
        addLine(builder, R.string.sentinel_last_update, data.lastUpdate)
        addLine(builder, R.string.sentinel_min_sdk, data.minSdk)
        addLine(builder, R.string.sentinel_target_sdk, data.targetSdk)
        addLine(builder, R.string.sentinel_package_name, data.packageName)
        addLine(builder, R.string.sentinel_process_name, data.processName)
        addLine(builder, R.string.sentinel_task_affinity, data.taskAffinity)
        addLine(builder, R.string.sentinel_locale_language, data.localeLanguage)
        addLine(builder, R.string.sentinel_locale_country, data.localeCountry)
    }

    internal fun addDeviceData(builder: StringBuilder, data: DeviceData) {
        addLine(builder, R.string.sentinel_manufacturer, data.manufacturer)
        addLine(builder, R.string.sentinel_model, data.model)
        addLine(builder, R.string.sentinel_id, data.id)
        addLine(builder, R.string.sentinel_bootloader, data.bootloader)
        addLine(builder, R.string.sentinel_device, data.device)
        addLine(builder, R.string.sentinel_board, data.board)
        addLine(builder, R.string.sentinel_architectures, data.architectures)
        addLine(builder, R.string.sentinel_codename, data.codename)
        addLine(builder, R.string.sentinel_release, data.release)
        addLine(builder, R.string.sentinel_sdk, data.sdk)
        addLine(builder, R.string.sentinel_security_patch, data.securityPatch)
        addLine(builder, R.string.sentinel_emulator, data.isProbablyAnEmulator.toString())
        addLine(builder, R.string.sentinel_auto_time, data.autoTime.toString())
        addLine(builder, R.string.sentinel_auto_timezone, data.autoTimezone.toString())
        addLine(builder, R.string.sentinel_rooted, data.isRooted.toString())
    }
}
