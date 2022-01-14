package com.infinum.sentinel.data.sources.raw.formatters.shared

import android.content.Context
import androidx.annotation.StringRes
import com.infinum.sentinel.R
import com.infinum.sentinel.data.models.local.CrashEntity
import com.infinum.sentinel.data.models.raw.ApplicationData
import com.infinum.sentinel.data.models.raw.DeviceData

internal abstract class StringBuilderFormatter {

    abstract fun addLine(builder: StringBuilder, @StringRes tag: Int, text: String)

    abstract fun addLine(builder: StringBuilder, tag: String, text: String)

    @Suppress("LongParameterList")
    internal fun addAllData(
        builder: StringBuilder,
        applicationData: String? = null,
        permissionsData: String? = null,
        deviceData: String? = null,
        preferencesData: String? = null,
        crashData: String? = null
    ): String =
        builder
            .apply {
                applicationData?.let { appendLine(it) }
                permissionsData?.let { appendLine(it) }
                deviceData?.let { appendLine(it) }
                preferencesData?.let { appendLine(it) }
                crashData?.let { appendLine(it) }
            }
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
        addLine(builder, R.string.sentinel_screen_width, data.screenWidth)
        addLine(builder, R.string.sentinel_screen_height, data.screenHeight)
        addLine(builder, R.string.sentinel_screen_size, data.screenSize)
        addLine(builder, R.string.sentinel_screen_density, data.screenDpi)
        addLine(builder, R.string.sentinel_font_scale, data.fontScale.toString())
    }

    internal fun addAnrData(context: Context, builder: StringBuilder, entity: CrashEntity) {
        addLine(builder, R.string.sentinel_timestamp, entity.timestamp.toString())
        addLine(builder, R.string.sentinel_message, context.getString(R.string.sentinel_anr_message))
        addLine(builder, R.string.sentinel_exception_name, context.getString(R.string.sentinel_anr_title))
    }

    internal fun addCrashData(builder: StringBuilder, entity: CrashEntity) {
        addLine(builder, R.string.sentinel_file, entity.data.exception?.file.orEmpty())
        addLine(builder, R.string.sentinel_line, entity.data.exception?.lineNumber?.toString().orEmpty())
        addLine(builder, R.string.sentinel_timestamp, entity.timestamp.toString())
        addLine(builder, R.string.sentinel_exception_name, entity.data.exception?.name.orEmpty())
        addLine(
            builder,
            R.string.sentinel_stacktrace,
            entity.data.exception?.asPrint().orEmpty()
        )
        addLine(builder, R.string.sentinel_thread_name, entity.data.thread?.name.orEmpty())
        addLine(builder, R.string.sentinel_thread_state, entity.data.thread?.state.orEmpty())
        addLine(
            builder,
            R.string.sentinel_priority,
            when (entity.data.thread?.priority) {
                Thread.MAX_PRIORITY -> "maximum"
                Thread.MIN_PRIORITY -> "minimum"
                else -> "normal"
            }
        )
        addLine(builder, R.string.sentinel_id, entity.data.thread?.id?.toString().orEmpty())
        addLine(builder, R.string.sentinel_daemon, entity.data.thread?.isDaemon?.toString().orEmpty())
    }
}
