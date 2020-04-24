package com.infinum.sentinel.ui.formatters

import android.content.Context
import androidx.annotation.StringRes
import com.infinum.sentinel.R
import com.infinum.sentinel.data.sources.raw.ApplicationCollector
import com.infinum.sentinel.data.sources.raw.DeviceCollector
import com.infinum.sentinel.data.sources.raw.PermissionsCollector
import com.infinum.sentinel.extensions.sanitize

internal class HtmlStringBuilder(
    private val context: Context,
    private val applicationCollector: ApplicationCollector,
    private val permissionsCollector: PermissionsCollector,
    private val deviceCollector: DeviceCollector
) : AbstractFormattedStringBuilder() {

    companion object {
        private const val HTML_START = "<html>"
        private const val HTML_END = "</html>"
        private const val BODY_START = "<body>"
        private const val BODY_END = "</body>"
        private const val PARAGRAPH_START = "<p>"
        private const val PARAGRAPH_END = "</p>"
        private const val BOLD_START = "<b>"
        private const val BOLD_END = "</b>"
        private const val DIV_START = "<div>"
        private const val DIV_END = "</div>"
        private const val UL_START = "<ul>"
        private const val UL_END = "</ul>"
        private const val LI_START = "<li>"
        private const val LI_END = "</li>"
        private const val FORMAT_BLOCK = "%s: %s"
    }

    override fun format(): String =
        StringBuilder()
            .appendln(HTML_START)
            .appendln(BODY_START)
            .appendln("$PARAGRAPH_START$BOLD_START$APPLICATION$BOLD_END$PARAGRAPH_END")
            .apply {
                applicationCollector.present().let {
                    addDiv(R.string.sentinel_version_code, it.versionCode)
                    addDiv(R.string.sentinel_version_name, it.versionName)
                    addDiv(R.string.sentinel_first_install, it.firstInstall)
                    addDiv(R.string.sentinel_last_update, it.lastUpdate)
                    addDiv(R.string.sentinel_min_sdk, it.minSdk)
                    addDiv(R.string.sentinel_target_sdk, it.targetSdk)
                    addDiv(R.string.sentinel_package_name, it.packageName)
                    addDiv(R.string.sentinel_process_name, it.processName)
                    addDiv(R.string.sentinel_task_affinity, it.taskAffinity)
                    addDiv(R.string.sentinel_locale_language, it.localeLanguage)
                    addDiv(R.string.sentinel_locale_country, it.localeCountry)
                }
            }
            .appendln("$PARAGRAPH_START$BOLD_START$PERMISSIONS$BOLD_END$PARAGRAPH_END")
            .appendln(UL_START)
            .apply {
                permissionsCollector.present().let {
                    it.forEach { entry ->
                        addLi(entry.key, entry.value.toString())
                    }
                }
            }
            .appendln(UL_END)
            .appendln("$PARAGRAPH_START$BOLD_START$DEVICE$BOLD_END$PARAGRAPH_END")
            .apply {
                deviceCollector.present().let {
                    addDiv(R.string.sentinel_manufacturer, it.manufacturer)
                    addDiv(R.string.sentinel_model, it.model)
                    addDiv(R.string.sentinel_id, it.id)
                    addDiv(R.string.sentinel_bootloader, it.bootloader)
                    addDiv(R.string.sentinel_device, it.device)
                    addDiv(R.string.sentinel_board, it.board)
                    addDiv(R.string.sentinel_architectures, it.architectures)
                    addDiv(R.string.sentinel_codename, it.codename)
                    addDiv(R.string.sentinel_release, it.release)
                    addDiv(R.string.sentinel_sdk, it.sdk)
                    addDiv(R.string.sentinel_security_patch, it.securityPatch)
                    addDiv(R.string.sentinel_emulator, it.isProbablyAnEmulator.toString())
                }
            }
            .appendln(BODY_END)
            .appendln(HTML_END)
            .toString()

    private fun StringBuilder.addDiv(@StringRes tag: Int, text: String) {
        context.getString(tag).sanitize().let {
            appendln(String.format(FORMAT_BLOCK, "$DIV_START$it", "$text$DIV_END"))
        }
    }

    private fun StringBuilder.addLi(tag: String, text: String) {
        appendln(String.format(FORMAT_BLOCK, "$LI_START$tag", "$text$LI_END"))
    }
}
