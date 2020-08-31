package com.infinum.sentinel.ui.formatters

import android.content.Context
import androidx.annotation.StringRes
import com.infinum.sentinel.R
import com.infinum.sentinel.extensions.sanitize

internal class HtmlStringBuilder(
    private val context: Context
) : AbstractFormattedStringBuilder<String, String>(context) {

    companion object {
        private const val HTML_START = "<html>"
        private const val HTML_END = "</html>"
        private const val BODY_START = "<body>"
        private const val BODY_END = "</body>"
        private const val HEADING_START = "<h1>"
        private const val HEADING_END = "</h1>"
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

    override fun invoke(): String =
        StringBuilder()
            .appendLine(HTML_START)
            .appendLine(BODY_START)
            .appendLine(application())
            .appendLine(permissions())
            .appendLine(device())
            .appendLine(preferences())
            .appendLine(BODY_END)
            .appendLine(HTML_END)
            .toString()

    override fun application(): String =
        StringBuilder()
            .appendLine("$HEADING_START$BOLD_START$APPLICATION$BOLD_END$HEADING_END")
            .apply {
                applicationCollector().let {
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
            .toString()

    override fun permissions(): String =
        StringBuilder()
            .appendLine("$HEADING_START$BOLD_START$PERMISSIONS$BOLD_END$HEADING_END")
            .appendLine(UL_START)
            .apply {
                permissionsCollector().let {
                    it.forEach { entry ->
                        addLi(entry.key, entry.value.toString())
                    }
                }
            }
            .appendLine(UL_END)
            .toString()

    override fun device(): String =
        StringBuilder()
            .appendLine("$HEADING_START$BOLD_START$DEVICE$BOLD_END$HEADING_END")
            .apply {
                deviceCollector().let {
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
            .toString()

    @Suppress("NestedBlockDepth")
    override fun preferences(): String =
        StringBuilder()
            .appendLine("$HEADING_START$BOLD_START$PREFERENCES$BOLD_END$HEADING_END")
            .apply {
                preferencesCollector().let {
                    it.forEach { preference ->
                        appendLine("$PARAGRAPH_START${preference.name}$PARAGRAPH_END")
                        preference.values.forEach { triple ->
                            addLi(triple.second, triple.third.toString())
                        }
                    }
                }
            }
            .toString()

    private fun StringBuilder.addDiv(@StringRes tag: Int, text: String) {
        context.getString(tag).sanitize().let {
            appendLine(String.format(FORMAT_BLOCK, "$DIV_START$it", "$text$DIV_END"))
        }
    }

    private fun StringBuilder.addLi(tag: String, text: String) {
        appendLine(String.format(FORMAT_BLOCK, "$LI_START$tag", "$text$LI_END"))
    }
}
