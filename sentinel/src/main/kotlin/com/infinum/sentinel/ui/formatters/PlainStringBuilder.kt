package com.infinum.sentinel.ui.formatters

import android.content.Context
import androidx.annotation.StringRes
import com.infinum.sentinel.R
import com.infinum.sentinel.extensions.sanitize

internal class PlainStringBuilder(
    private val context: Context
) : AbstractFormattedStringBuilder<String, String>(context) {

    companion object {
        private const val SEPARATOR = "-"
    }

    override fun format(): String =
        StringBuilder()
            .appendLine(application())
            .appendLine(permissions())
            .appendLine(device())
            .appendLine(preferences())
            .toString()

    override fun application(): String =
        StringBuilder()
            .appendLine(APPLICATION.toUpperCase())
            .appendLine(SEPARATOR.repeat(APPLICATION.length))
            .apply {
                applicationCollector.present().let {
                    addLine(R.string.sentinel_version_code, it.versionCode)
                    addLine(R.string.sentinel_version_name, it.versionName)
                    addLine(R.string.sentinel_first_install, it.firstInstall)
                    addLine(R.string.sentinel_last_update, it.lastUpdate)
                    addLine(R.string.sentinel_min_sdk, it.minSdk)
                    addLine(R.string.sentinel_target_sdk, it.targetSdk)
                    addLine(R.string.sentinel_package_name, it.packageName)
                    addLine(R.string.sentinel_process_name, it.processName)
                    addLine(R.string.sentinel_task_affinity, it.taskAffinity)
                    addLine(R.string.sentinel_locale_language, it.localeLanguage)
                    addLine(R.string.sentinel_locale_country, it.localeCountry)
                }
            }
            .appendLine()
            .toString()

    override fun permissions(): String =
        StringBuilder()
            .appendLine(PERMISSIONS.toUpperCase())
            .appendLine(SEPARATOR.repeat(PERMISSIONS.length))
            .apply {
                permissionsCollector.present().let {
                    it.forEach { entry ->
                        appendLine("${entry.key}: ${entry.value}")
                    }
                }
            }
            .appendLine()
            .toString()

    override fun device(): String =
        StringBuilder()
            .appendLine(DEVICE.toUpperCase())
            .appendLine(SEPARATOR.repeat(DEVICE.length))
            .apply {
                deviceCollector.present().let {
                    addLine(R.string.sentinel_manufacturer, it.manufacturer)
                    addLine(R.string.sentinel_model, it.model)
                    addLine(R.string.sentinel_id, it.id)
                    addLine(R.string.sentinel_bootloader, it.bootloader)
                    addLine(R.string.sentinel_device, it.device)
                    addLine(R.string.sentinel_board, it.board)
                    addLine(R.string.sentinel_architectures, it.architectures)
                    addLine(R.string.sentinel_codename, it.codename)
                    addLine(R.string.sentinel_release, it.release)
                    addLine(R.string.sentinel_sdk, it.sdk)
                    addLine(R.string.sentinel_security_patch, it.securityPatch)
                    addLine(R.string.sentinel_emulator, it.isProbablyAnEmulator.toString())
                }
            }
            .appendLine()
            .toString()

    @Suppress("NestedBlockDepth")
    override fun preferences(): String =
        StringBuilder()
            .appendLine(PREFERENCES.toUpperCase())
            .appendLine(SEPARATOR.repeat(PREFERENCES.length))
            .apply {
                preferencesCollector.present().let {
                    it.forEach { preference ->
                        appendLine()
                        appendLine(preference.name)
                        appendLine(SEPARATOR.repeat(preference.name.length))
                        preference.values.forEach { triple ->
                            addLine(triple.second, triple.third.toString())
                        }
                    }
                }
            }
            .toString()

    private fun StringBuilder.addLine(@StringRes tag: Int, text: String) {
        context.getString(tag).sanitize().let {
            appendLine("$it: $text")
        }
    }

    private fun StringBuilder.addLine(tag: String, text: String) {
        appendLine("$tag: $text")
    }
}
