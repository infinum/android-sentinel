package com.infinum.sentinel.data.sources.raw.formatters

import android.annotation.SuppressLint
import android.content.Context
import android.util.Xml
import androidx.annotation.StringRes
import com.infinum.sentinel.R
import com.infinum.sentinel.data.models.local.CrashEntity
import com.infinum.sentinel.data.sources.raw.formatters.Formatter.Companion.APPLICATION
import com.infinum.sentinel.data.sources.raw.formatters.Formatter.Companion.CRASH
import com.infinum.sentinel.data.sources.raw.formatters.Formatter.Companion.DEVICE
import com.infinum.sentinel.data.sources.raw.formatters.Formatter.Companion.NAME
import com.infinum.sentinel.data.sources.raw.formatters.Formatter.Companion.PERMISSIONS
import com.infinum.sentinel.data.sources.raw.formatters.Formatter.Companion.PREFERENCES
import com.infinum.sentinel.data.sources.raw.formatters.Formatter.Companion.STATUS
import com.infinum.sentinel.domain.collectors.Collectors
import com.infinum.sentinel.domain.formatters.Formatters
import com.infinum.sentinel.extensions.sanitize
import java.io.StringWriter
import org.xmlpull.v1.XmlSerializer

@Suppress("TooManyFunctions")
internal class XmlFormatter(
    private val context: Context,
    private val applicationCollector: Collectors.Application,
    private val permissionsCollector: Collectors.Permissions,
    private val deviceCollector: Collectors.Device,
    private val preferencesCollector: Collectors.Preferences
) : Formatters.Xml {

    companion object {
        private const val NAMESPACE = "sentinel"
        private const val ROOT = "sentinel"
        private const val PERMISSION = "permission"
        private const val PREFERENCE = "preference"
        private const val VALUE = "value"
        private const val FEATURE_INDENT = "http://xmlpull.org/v1/doc/features.html#indent-output"
    }

    @SuppressLint("DefaultLocale")
    override fun invoke(): String =
        StringWriter().apply {
            with(Xml.newSerializer()) {
                setFeature(FEATURE_INDENT, true)
                setOutput(this@apply)
                startDocument(Xml.Encoding.UTF_8.name, true)
                startTag(NAMESPACE, ROOT)
                addApplicationNode()
                addPermissionsNode()
                addDeviceNode()
                addPreferencesNode()
                endTag(NAMESPACE, ROOT)
                endDocument()
            }
        }.toString()

    override fun formatCrash(includeAllData: Boolean, entity: CrashEntity): String =
        if (includeAllData) {
            StringWriter().apply {
                with(Xml.newSerializer()) {
                    setFeature(FEATURE_INDENT, true)
                    setOutput(this@apply)
                    startDocument(Xml.Encoding.UTF_8.name, true)
                    startTag(NAMESPACE, ROOT)
                    addApplicationNode()
                    addPermissionsNode()
                    addDeviceNode()
                    addPreferencesNode()
                    addCrashNode(entity)
                    endTag(NAMESPACE, ROOT)
                    endDocument()
                }
            }.toString()
        } else {
            StringWriter().apply {
                with(Xml.newSerializer()) {
                    setFeature(FEATURE_INDENT, true)
                    setOutput(this@apply)
                    startDocument(Xml.Encoding.UTF_8.name, true)
                    startTag(NAMESPACE, ROOT)
                    addCrashNode(entity)
                    endTag(NAMESPACE, ROOT)
                    endDocument()
                }
            }.toString()
        }

    override fun application(): String = ""

    override fun permissions(): String = ""

    override fun device(): String = ""

    override fun preferences(): String = ""

    override fun crash(entity: CrashEntity): String = ""

    private fun XmlSerializer.addApplicationNode() {
        startTag(NAMESPACE, APPLICATION)
        applicationCollector().let {
            addNode(R.string.sentinel_version_code, it.versionCode)
            addNode(R.string.sentinel_version_name, it.versionName)
            addNode(R.string.sentinel_first_install, it.firstInstall)
            addNode(R.string.sentinel_last_update, it.lastUpdate)
            addNode(R.string.sentinel_min_sdk, it.minSdk)
            addNode(R.string.sentinel_target_sdk, it.targetSdk)
            addNode(R.string.sentinel_package_name, it.packageName)
            addNode(R.string.sentinel_process_name, it.processName)
            addNode(R.string.sentinel_task_affinity, it.taskAffinity)
            addNode(R.string.sentinel_locale_language, it.localeLanguage)
            addNode(R.string.sentinel_locale_country, it.localeCountry)
        }
        endTag(NAMESPACE, APPLICATION)
    }

    private fun XmlSerializer.addPermissionsNode() {
        startTag(NAMESPACE, PERMISSIONS)
        permissionsCollector().let {
            it.forEach { entry ->
                addNodeWithPermissionAttributes(entry.key, entry.value.toString())
            }
        }
        endTag(NAMESPACE, PERMISSIONS)
    }

    private fun XmlSerializer.addDeviceNode() {
        startTag(NAMESPACE, DEVICE)
        deviceCollector().let {
            addNode(R.string.sentinel_manufacturer, it.manufacturer)
            addNode(R.string.sentinel_model, it.model)
            addNode(R.string.sentinel_id, it.id)
            addNode(R.string.sentinel_bootloader, it.bootloader)
            addNode(R.string.sentinel_device, it.device)
            addNode(R.string.sentinel_board, it.board)
            addNode(R.string.sentinel_architectures, it.architectures)
            addNode(R.string.sentinel_codename, it.codename)
            addNode(R.string.sentinel_release, it.release)
            addNode(R.string.sentinel_sdk, it.sdk)
            addNode(R.string.sentinel_security_patch, it.securityPatch)
            addNode(R.string.sentinel_emulator, it.isProbablyAnEmulator.toString())
            addNode(R.string.sentinel_auto_time, it.autoTime.toString())
            addNode(R.string.sentinel_auto_timezone, it.autoTimezone.toString())
            addNode(R.string.sentinel_rooted, it.isRooted.toString())
        }
        endTag(NAMESPACE, DEVICE)
    }

    private fun XmlSerializer.addPreferencesNode() {
        startTag(NAMESPACE, PREFERENCES)
        preferencesCollector().let {
            it.forEach { preference ->
                startTag(NAMESPACE, PREFERENCE)
                attribute(NAMESPACE, NAME, preference.name)
                preference.values.forEach { triple ->
                    addNodeWithPreferenceAttributes(triple.second, triple.third.toString())
                }
                endTag(NAMESPACE, PREFERENCE)
            }
        }
        endTag(NAMESPACE, PREFERENCES)
    }

    private fun XmlSerializer.addCrashNode(entity: CrashEntity) {
        startTag(NAMESPACE, CRASH)
        if (entity.data.exception?.isANRException == true) {
            addNode(R.string.sentinel_timestamp, entity.timestamp.toString())
            addNode(R.string.sentinel_message, context.getString(R.string.sentinel_anr_message))
            addNode(R.string.sentinel_exception_name, context.getString(R.string.sentinel_anr_title))
            addNode(
                R.string.sentinel_stacktrace,
                "${entity.data.exception?.name}: ${entity.data.exception?.message}"
                    .plus(entity.data.exception?.stackTrace?.joinToString { "\n\t\t\t at $it" })
            )
            addNode(R.string.sentinel_thread_states, entity.data.threadState.orEmpty().count().toString())
            entity.data.threadState?.forEach { process ->
                addNode(
                    R.string.sentinel_stacktrace,
                    "${process.name}\t\t\t${process.state.uppercase()}"
                        .plus(process.stackTrace.joinToString { "\n\t\t\t at $it" })
                )
            }
        } else {
            addNode(R.string.sentinel_file, entity.data.exception?.file.orEmpty())
            addNode(R.string.sentinel_line, entity.data.exception?.lineNumber?.toString().orEmpty())
            addNode(R.string.sentinel_timestamp, entity.timestamp.toString())
            addNode(R.string.sentinel_exception_name, entity.data.exception?.name.orEmpty())
            addNode(
                R.string.sentinel_stacktrace,
                "${entity.data.exception?.name}: ${entity.data.exception?.message}"
                    .plus(entity.data.exception?.stackTrace?.joinToString { "\n\t\t\t at $it" })
            )
            addNode(R.string.sentinel_thread_name, entity.data.thread?.name.orEmpty())
            addNode(R.string.sentinel_thread_state, entity.data.thread?.state.orEmpty())
            addNode(
                R.string.sentinel_priority,
                when (entity.data.thread?.priority) {
                    Thread.MAX_PRIORITY -> "maximum"
                    Thread.MIN_PRIORITY -> "minimum"
                    else -> "normal"
                }
            )
            addNode(R.string.sentinel_id, entity.data.thread?.id?.toString().orEmpty())
            addNode(R.string.sentinel_daemon, entity.data.thread?.isDaemon?.toString().orEmpty())
        }
        endTag(NAMESPACE, CRASH)
    }

    private fun XmlSerializer.addNode(@StringRes tag: Int, text: String) {
        context.getString(tag).sanitize().let {
            startTag(NAMESPACE, it)
            text(text)
            endTag(NAMESPACE, it)
        }
    }

    private fun XmlSerializer.addNodeWithPermissionAttributes(name: String, status: String) {
        startTag(NAMESPACE, PERMISSION)
        attribute(NAMESPACE, NAME, name)
        attribute(NAMESPACE, STATUS, status)
        endTag(NAMESPACE, PERMISSION)
    }

    private fun XmlSerializer.addNodeWithPreferenceAttributes(name: String, value: String) {
        startTag(NAMESPACE, VALUE)
        attribute(NAMESPACE, NAME, name)
        attribute(NAMESPACE, VALUE, value)
        endTag(NAMESPACE, VALUE)
    }
}
