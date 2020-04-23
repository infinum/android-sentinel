package com.infinum.sentinel.ui.formatters

import android.annotation.SuppressLint
import android.content.Context
import android.util.Xml
import androidx.annotation.StringRes
import com.infinum.sentinel.R
import com.infinum.sentinel.data.sources.raw.ApplicationCollector
import com.infinum.sentinel.data.sources.raw.DeviceCollector
import com.infinum.sentinel.data.sources.raw.PermissionsCollector
import com.infinum.sentinel.extensions.sanitize
import org.xmlpull.v1.XmlSerializer
import java.io.StringWriter

internal class XmlStringBuilder(
    private val context: Context,
    private val applicationCollector: ApplicationCollector,
    private val permissionsCollector: PermissionsCollector,
    private val deviceCollector: DeviceCollector
) : AbstractFormattedStringBuilder() {

    companion object {
        private const val NAMESPACE = "sentinel"
        private const val ROOT = "sentinel"
        private const val PERMISSION = "permission"
    }

    @SuppressLint("DefaultLocale")
    override fun format(): String =
        StringWriter().apply {
            with(Xml.newSerializer()) {
                setOutput(this@apply)
                startDocument(Charsets.UTF_8.name().toUpperCase(), true)

                startTag(NAMESPACE, ROOT)

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

                startTag(NAMESPACE, PERMISSIONS)
                permissionsCollector.present().let {
                    it.forEach { entry ->
                        addNodeWithAttribute(entry.key, entry.value.toString())
                    }
                }
                endTag(NAMESPACE, PERMISSIONS)

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
                }
                endTag(NAMESPACE, DEVICE)

                endTag(NAMESPACE, ROOT)

                endDocument()
            }
        }.toString()

    private fun XmlSerializer.addNode(@StringRes tag: Int, text: String) {
        context.getString(tag).sanitize().let {
            startTag(NAMESPACE, it)
            text(text)
            endTag(NAMESPACE, it)
        }
    }

    private fun XmlSerializer.addNodeWithAttribute(name: String, status: String) {
        startTag(NAMESPACE, PERMISSION)
        attribute(NAMESPACE, NAME, name)
        attribute(NAMESPACE, STATUS, status)
        endTag(NAMESPACE, PERMISSION)
    }
}
