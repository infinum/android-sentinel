package com.infinum.sentinel.data.sources.raw.formatters

import android.content.Context
import com.infinum.sentinel.R
import com.infinum.sentinel.data.models.local.CrashEntity
import com.infinum.sentinel.data.sources.raw.formatters.Formatter.Companion.APPLICATION
import com.infinum.sentinel.data.sources.raw.formatters.Formatter.Companion.CRASH
import com.infinum.sentinel.data.sources.raw.formatters.Formatter.Companion.DEVICE
import com.infinum.sentinel.data.sources.raw.formatters.Formatter.Companion.PERMISSIONS
import com.infinum.sentinel.data.sources.raw.formatters.Formatter.Companion.PREFERENCES
import com.infinum.sentinel.data.sources.raw.formatters.shared.StringBuilderFormatter
import com.infinum.sentinel.domain.collectors.Collectors
import com.infinum.sentinel.domain.formatters.Formatters
import com.infinum.sentinel.extensions.sanitize
import java.util.Locale
import me.tatarka.inject.annotations.Inject

@Inject
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
        format()

    override fun formatCrash(includeAllData: Boolean, entity: CrashEntity): String =
        when (includeAllData) {
            true -> format(entity)
            false -> addAllData(
                builder = StringBuilder(),
                crashData = crash(entity)
            )
        }

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

    override fun crash(entity: CrashEntity): String =
        StringBuilder()
            .appendLine(CRASH.uppercase(Locale.getDefault()))
            .appendLine(SEPARATOR.repeat(CRASH.length))
            .apply {
                if (entity.data.exception?.isANRException == true) {
                    addAnrData(context, this, entity)
                    addLine(
                        this,
                        R.string.sentinel_stacktrace,
                        entity.data.exception?.asPrint().orEmpty()
                    )
                    addLine(this, R.string.sentinel_thread_states, entity.data.threadState.orEmpty().count().toString())
                    entity.data.threadState?.forEach { process ->
                        appendLine()
                        appendLine(SEPARATOR.repeat(process.name.length))
                        addLine(
                            this,
                            context.getString(R.string.sentinel_stacktrace),
                            "${process.name}\t\t\t${process.state.uppercase()}"
                                .plus(process.stackTrace.joinToString { "\n\t\t\t at $it" })
                        )
                    }
                } else {
                    addCrashData(this, entity)
                }
            }
            .appendLine()
            .toString()

    private fun format(crash: CrashEntity? = null) =
        addAllData(
            StringBuilder(),
            application(),
            permissions(),
            device(),
            preferences(),
            crash?.let { crash(it) }
        )
}
