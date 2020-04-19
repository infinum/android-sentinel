package com.infinum.sentinel.data.sources.memory

import com.infinum.sentinel.data.sources.raw.DataSource

class PlainStringBuilder : AbstractFormattedStringBuilder() {

    companion object {
        private const val SEPARATOR = "-"
    }

    override fun format(): String =
        StringBuilder()
            .appendln(APPLICATION.toUpperCase())
            .appendln(SEPARATOR.repeat(APPLICATION.length))
            .apply {
                DataSource.applicationData.forEach {
                    appendln("${it.key.name.toUpperCase().replace("_", " ")}: ${it.value}")
                }
            }
            .appendln()
            .appendln(PERMISSIONS.toUpperCase())
            .appendln(SEPARATOR.repeat(PERMISSIONS.length))
            .apply {
                DataSource.permissions.forEach {
                    appendln("${it.key}: ${it.value}")
                }
            }
            .appendln()
            .appendln(DEVICE.toUpperCase())
            .appendln(SEPARATOR.repeat(DEVICE.length))
            .apply {
                DataSource.deviceData.forEach {
                    appendln("${it.key.name.toUpperCase().replace("_", " ")}: ${it.value}")
                }
            }
            .toString()
}