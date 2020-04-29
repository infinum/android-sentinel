package com.infinum.sentinel.ui.formatters

import android.annotation.SuppressLint
import android.content.Context
import android.util.Xml
import androidx.annotation.StringRes
import com.infinum.sentinel.R
import com.infinum.sentinel.extensions.sanitize
import org.xmlpull.v1.XmlSerializer
import java.io.StringWriter

@Suppress("TooManyFunctions")
internal class XmlStringBuilder(
    private val context: Context
) : AbstractFormattedStringBuilder(context) {

    companion object {
        private const val NAMESPACE = "sentinel"
        private const val ROOT = "sentinel"
        private const val PERMISSION = "permission"
        private const val PREFERENCE = "preference"
        private const val VALUE = "value"
    }

    @SuppressLint("DefaultLocale")
    override fun format(): String =
        StringWriter().apply {
            with(Xml.newSerializer()) {
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

    override fun application(): String = ""

    override fun permissions(): String = ""

    override fun device(): String = ""

    override fun preferences(): String = ""

    private fun XmlSerializer.addApplicationNode() {
        startTag(NAMESPACE, APPLICATION)
        applicationCollector.present().let {
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
        permissionsCollector.present().let {
            it.forEach { entry ->
                addNodeWithPermissionAttributes(entry.key, entry.value.toString())
            }
        }
        endTag(NAMESPACE, PERMISSIONS)
    }

    private fun XmlSerializer.addDeviceNode() {
        startTag(NAMESPACE, DEVICE)
        deviceCollector.present().let {
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
        }
        endTag(NAMESPACE, DEVICE)
    }

    private fun XmlSerializer.addPreferencesNode() {
        startTag(NAMESPACE, PREFERENCES)
        preferencesCollector.present().let {
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
