package com.infinum.sentinel.data.sources.memory

import com.infinum.sentinel.data.sources.raw.DataSource

class MarkdownStringBuilder : AbstractFormattedStringBuilder() {

    companion object {
        private const val HEADER = "# "
        private const val ITALIC = "_"
        private const val BULLET = "- "
    }

    override fun format(): String =
        StringBuilder()
            .appendln("$HEADER$APPLICATION")
            .apply {
                DataSource.applicationData.forEach {
                    appendln("$ITALIC${it.key.name.toLowerCase()}$ITALIC: ${it.value}")
                }
            }
            .appendln("$HEADER$PERMISSIONS")
            .apply {
                DataSource.permissions.forEach {
                    appendln("$BULLET$ITALIC${it.key}$ITALIC: ${it.value}")
                }
            }
            .appendln("$HEADER$DEVICE")
            .apply {
                DataSource.deviceData.forEach {
                    appendln("$ITALIC${it.key.name.toLowerCase()}$ITALIC: ${it.value}")
                }
            }
            .toString()
}