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
import me.tatarka.inject.annotations.Inject

@Inject
internal class MarkdownFormatter(
    private val context: Context,
    private val applicationCollector: Collectors.Application,
    private val permissionsCollector: Collectors.Permissions,
    private val deviceCollector: Collectors.Device,
    private val preferencesCollector: Collectors.Preferences
) : StringBuilderFormatter(), Formatters.Markdown {

    companion object {
        private const val HEADER_1 = "# "
        private const val HEADER_2 = "## "
        private const val ITALIC = "_"
        private const val BULLET = "- "
    }

    override fun addLine(builder: StringBuilder, tag: Int, text: String) {
        context.getString(tag).sanitize().let {
            builder.appendLine("$ITALIC${it}$ITALIC: $text")
        }
    }

    override fun addLine(builder: StringBuilder, tag: String, text: String) {
        builder.appendLine("$ITALIC${tag}$ITALIC: $text")
    }

    override fun invoke(): String =
        format()

    override fun formatCrash(includeAllData: Boolean, entity: CrashEntity): String =
        if (includeAllData) {
            format(entity)
        } else {
            addAllData(
                builder = StringBuilder(),
                crashData = crash(entity)
            )
        }

    override fun application(): String =
        StringBuilder()
            .appendLine("$HEADER_1$APPLICATION")
            .apply { addApplicationData(this, applicationCollector()) }
            .toString()

    override fun permissions(): String =
        StringBuilder()
            .appendLine("$HEADER_1$PERMISSIONS")
            .apply {
                permissionsCollector().let {
                    it.forEach { entry ->
                        appendLine("$BULLET$ITALIC${entry.key}$ITALIC: ${entry.value}")
                    }
                }
            }
            .toString()

    override fun device(): String =
        StringBuilder()
            .appendLine("$HEADER_1$DEVICE")
            .apply { addDeviceData(this, deviceCollector()) }
            .toString()

    @Suppress("NestedBlockDepth")
    override fun preferences(): String =
        StringBuilder()
            .appendLine("$HEADER_1$PREFERENCES")
            .apply {
                preferencesCollector().let {
                    it.forEach { preference ->
                        appendLine("$HEADER_2${preference.name}")
                        preference.values.forEach { triple ->
                            appendLine("$BULLET$ITALIC${triple.second}$ITALIC: ${triple.third}")
                        }
                    }
                }
            }
            .toString()

    override fun crash(entity: CrashEntity): String =
        StringBuilder()
            .appendLine("$HEADER_1$CRASH")
            .apply {
                if (entity.data.exception?.isANRException == true) {
                    addAnrData(context, this, entity)
                    addLine(
                        this,
                        R.string.sentinel_stacktrace,
                        "\n>> ${entity.data.exception?.name}: ${entity.data.exception?.message}"
                            .plus(
                                entity
                                    .data
                                    .exception
                                    ?.stackTrace
                                    ?.joinToString { "\n>> &nbsp;&nbsp;&nbsp;&nbsp; at $it" }
                            )
                    )
                    appendLine("\n")
                    addLine(this, R.string.sentinel_thread_states, entity.data.threadState.orEmpty().count().toString())
                    entity.data.threadState?.forEach { process ->
                        appendLine("\n")
                        addLine(
                            this,
                            R.string.sentinel_stacktrace,
                            "\n>> ${process.name}&nbsp;&nbsp;&nbsp;&nbsp;${process.state.uppercase()}"
                                .plus(process.stackTrace.joinToString { "\n>> &nbsp;&nbsp;&nbsp;&nbsp; at $it" })
                        )
                    }
                } else {
                    addCrashData(this, entity)
                }
            }
            .toString()

    private fun format(entity: CrashEntity? = null) =
        addAllData(
            StringBuilder(),
            application(),
            permissions(),
            device(),
            preferences(),
            entity?.let { crash(it) }
        )
}
