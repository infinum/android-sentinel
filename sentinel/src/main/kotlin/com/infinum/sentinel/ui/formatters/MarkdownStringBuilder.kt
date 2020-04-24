package com.infinum.sentinel.ui.formatters

import android.content.Context
import androidx.annotation.StringRes
import com.infinum.sentinel.R
import com.infinum.sentinel.data.sources.raw.ApplicationCollector
import com.infinum.sentinel.data.sources.raw.DeviceCollector
import com.infinum.sentinel.data.sources.raw.PermissionsCollector
import com.infinum.sentinel.data.sources.raw.PreferencesCollector
import com.infinum.sentinel.extensions.sanitize

internal class MarkdownStringBuilder(
    private val context: Context,
    private val applicationCollector: ApplicationCollector,
    private val permissionsCollector: PermissionsCollector,
    private val deviceCollector: DeviceCollector,
    private val preferencesCollector: PreferencesCollector
) : AbstractFormattedStringBuilder() {

    companion object {
        private const val HEADER_1 = "# "
        private const val HEADER_2 = "## "
        private const val ITALIC = "_"
        private const val BULLET = "- "
    }

    override fun format(): String =
        StringBuilder()
            .appendln(application())
            .appendln(permissions())
            .appendln(device())
            .appendln(preferences())
            .toString()

    override fun application(): String =
        StringBuilder()
            .appendln("$HEADER_1$APPLICATION")
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
            .toString()

    override fun permissions(): String =
        StringBuilder()
            .appendln("$HEADER_1$PERMISSIONS")
            .apply {
                permissionsCollector.present().let {
                    it.forEach { entry ->
                        addListItem(entry.key, entry.value.toString())
                    }
                }
            }
            .toString()

    override fun device(): String =
        StringBuilder()
            .appendln("$HEADER_1$DEVICE")
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
            .toString()

    @Suppress("NestedBlockDepth")
    override fun preferences(): String =
        StringBuilder()
            .appendln("$HEADER_1$PREFERENCES")
            .apply {
                preferencesCollector.present().let {
                    it.forEach { preference ->
                        addHeader2(preference.name)
                        preference.values.forEach { triple ->
                            addListItem(triple.second, triple.third.toString())
                        }
                    }
                }
            }
            .toString()

    private fun StringBuilder.addHeader2(name: String) {
        appendln("$HEADER_2$name")
    }

    private fun StringBuilder.addLine(@StringRes tag: Int, text: String) {
        context.getString(tag).sanitize().let {
            appendln("$ITALIC${it}$ITALIC: $text")
        }
    }

    private fun StringBuilder.addListItem(name: String, text: String) {
        appendln("$BULLET$ITALIC${name}$ITALIC: $text")
    }
}
