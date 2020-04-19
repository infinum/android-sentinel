package com.infinum.sentinel.data.sources.memory

import com.infinum.sentinel.data.sources.raw.DataSource

class HtmlStringBuilder : AbstractFormattedStringBuilder() {

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
    }

    override fun format(): String =
        StringBuilder()
            .appendln(HTML_START)
            .appendln(BODY_START)
            .appendln("$PARAGRAPH_START$BOLD_START$APPLICATION$BOLD_END$PARAGRAPH_END")
            .apply {
                DataSource.applicationData.forEach {
                    appendln("$DIV_START${it.key.name.toLowerCase()}: ${it.value}$DIV_END")
                }
            }
            .appendln("$PARAGRAPH_START$BOLD_START$PERMISSIONS$BOLD_END$PARAGRAPH_END")
            .appendln(UL_START)
            .apply {
                DataSource.permissions.forEach {
                    appendln("$LI_START${it.key}: ${it.value}$LI_END")
                }
            }
            .appendln(UL_END)
            .appendln("$PARAGRAPH_START$BOLD_START$DEVICE$BOLD_END$PARAGRAPH_END")
            .apply {
                DataSource.deviceData.forEach {
                    appendln("$DIV_START${it.key.name.toLowerCase()}: ${it.value}$DIV_END")
                }
            }
            .appendln(BODY_END)
            .appendln(HTML_END)
            .toString()
}