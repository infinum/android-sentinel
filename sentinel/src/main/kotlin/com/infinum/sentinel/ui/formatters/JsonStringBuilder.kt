package com.infinum.sentinel.ui.formatters

import android.content.Context
import androidx.annotation.StringRes
import com.infinum.sentinel.R
import com.infinum.sentinel.data.sources.raw.ApplicationCollector
import com.infinum.sentinel.data.sources.raw.DeviceCollector
import com.infinum.sentinel.data.sources.raw.PermissionsCollector
import com.infinum.sentinel.extensions.sanitize
import org.json.JSONArray
import org.json.JSONObject

internal class JsonStringBuilder(
    private val context: Context,
    private val applicationCollector: ApplicationCollector,
    private val permissionsCollector: PermissionsCollector,
    private val deviceCollector: DeviceCollector
) : AbstractFormattedStringBuilder() {

    override fun format(): String =
        JSONObject()
            .put(APPLICATION, JSONObject().apply {
                applicationCollector.present().let {
                    addKey(R.string.sentinel_version_code, it.versionCode)
                    addKey(R.string.sentinel_version_name, it.versionName)
                    addKey(R.string.sentinel_first_install, it.firstInstall)
                    addKey(R.string.sentinel_last_update, it.lastUpdate)
                    addKey(R.string.sentinel_min_sdk, it.minSdk)
                    addKey(R.string.sentinel_target_sdk, it.targetSdk)
                    addKey(R.string.sentinel_package_name, it.packageName)
                    addKey(R.string.sentinel_process_name, it.processName)
                    addKey(R.string.sentinel_task_affinity, it.taskAffinity)
                    addKey(R.string.sentinel_locale_language, it.localeLanguage)
                    addKey(R.string.sentinel_locale_country, it.localeCountry)
                }
            })
            .put(PERMISSIONS, JSONArray().apply {
                permissionsCollector.present().let {
                    it.forEach { entry ->
                        put(
                            JSONObject().apply {
                                addKey(NAME, entry.key)
                                addKey(STATUS, entry.value.toString())
                            }
                        )
                    }
                }
            })
            .put(DEVICE, JSONObject().apply {
                deviceCollector.present().let {
                    addKey(R.string.sentinel_manufacturer, it.manufacturer)
                    addKey(R.string.sentinel_model, it.model)
                    addKey(R.string.sentinel_id, it.id)
                    addKey(R.string.sentinel_bootloader, it.bootloader)
                    addKey(R.string.sentinel_device, it.device)
                    addKey(R.string.sentinel_board, it.board)
                    addKey(R.string.sentinel_architectures, it.architectures)
                    addKey(R.string.sentinel_codename, it.codename)
                    addKey(R.string.sentinel_release, it.release)
                    addKey(R.string.sentinel_sdk, it.sdk)
                    addKey(R.string.sentinel_security_patch, it.securityPatch)
                    addKey(R.string.sentinel_emulator, it.isProbablyAnEmulator)
                }
            })
            .toString()

    private fun JSONObject.addKey(@StringRes key: Int, value: String) {
        context.getString(key).sanitize().let {
            put(it, value)
        }
    }

    private fun JSONObject.addKey(@StringRes key: Int, value: Boolean) {
        context.getString(key).sanitize().let {
            put(it, value)
        }
    }

    private fun JSONObject.addKey(key: String, value: String) {
        put(key, value)
    }
}
