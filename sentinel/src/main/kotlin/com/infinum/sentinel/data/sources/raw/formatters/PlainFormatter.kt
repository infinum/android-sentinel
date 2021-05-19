package com.infinum.sentinel.data.sources.raw.formatters

import android.content.Context
import com.infinum.sentinel.data.sources.raw.formatters.Formatter.Companion.APPLICATION
import com.infinum.sentinel.data.sources.raw.formatters.Formatter.Companion.DEVICE
import com.infinum.sentinel.data.sources.raw.formatters.Formatter.Companion.PERMISSIONS
import com.infinum.sentinel.data.sources.raw.formatters.Formatter.Companion.PREFERENCES
import com.infinum.sentinel.data.sources.raw.formatters.shared.StringBuilderFormatter
import com.infinum.sentinel.domain.collectors.Collectors
import com.infinum.sentinel.domain.formatters.Formatters
import com.infinum.sentinel.extensions.sanitize
import java.util.Locale

internal class PlainFormatter(
    private val context: Context,
    private val applicationCollector: Collectors.Application,
    private val permissionsCollector: Collectors.Permissions,
    private val deviceCollector: Collectors.Device,
    private val preferencesCollector: Collectors.Preferences
) : StringBuilderFormatter(), Formatters.Plain {

    companion object {
        private const val SEPARATOR = "-"
    }

    override fun addLine(builder: StringBuilder, tag: String, text: String) {
        builder.appendLine("$tag: $text")
    }

    override fun addLine(builder: StringBuilder, tag: Int, text: String) {
        context.getString(tag).sanitize().let {
            builder.appendLine("$it: $text")
        }
    }

    override fun invoke(): String =
        addAllData(
            StringBuilder(),
            application(),
            permissions(),
            device(),
            preferences()
        )

    override fun application(): String =
        StringBuilder()
            .appendLine(APPLICATION.uppercase(Locale.getDefault()))
            .appendLine(SEPARATOR.repeat(APPLICATION.length))
            .apply { addApplicationData(this, applicationCollector()) }
            .appendLine()
            .toString()

    override fun permissions(): String =
        StringBuilder()
            .appendLine(PERMISSIONS.uppercase(Locale.getDefault()))
            .appendLine(SEPARATOR.repeat(PERMISSIONS.length))
            .apply {
                permissionsCollector().let {
                    it.forEach { entry ->
                        appendLine("${entry.key}: ${entry.value}")
                    }
                }
            }
            .appendLine()
            .toString()

    override fun device(): String =
        StringBuilder()
            .appendLine(DEVICE.uppercase(Locale.getDefault()))
            .appendLine(SEPARATOR.repeat(DEVICE.length))
            .apply { addDeviceData(this, deviceCollector()) }
            .appendLine()
            .toString()

    @Suppress("NestedBlockDepth")
    override fun preferences(): String =
        StringBuilder()
            .appendLine(PREFERENCES.uppercase(Locale.getDefault()))
            .appendLine(SEPARATOR.repeat(PREFERENCES.length))
            .apply {
                preferencesCollector().let {
                    it.forEach { preference ->
                        appendLine()
                        appendLine(preference.name)
                        appendLine(SEPARATOR.repeat(preference.name.length))
                        preference.values.forEach { triple ->
                            addLine(this, triple.second, triple.third.toString())
                        }
                    }
                }
            }
            .toString()
}
