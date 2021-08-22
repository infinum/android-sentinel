package com.infinum.sentinel.data.sources.raw.formatters

import android.content.Context
import androidx.annotation.StringRes
import com.infinum.sentinel.R
import com.infinum.sentinel.data.sources.raw.formatters.Formatter.Companion.APPLICATION
import com.infinum.sentinel.data.sources.raw.formatters.Formatter.Companion.DEVICE
import com.infinum.sentinel.data.sources.raw.formatters.Formatter.Companion.NAME
import com.infinum.sentinel.data.sources.raw.formatters.Formatter.Companion.PERMISSIONS
import com.infinum.sentinel.data.sources.raw.formatters.Formatter.Companion.PREFERENCES
import com.infinum.sentinel.data.sources.raw.formatters.Formatter.Companion.STATUS
import com.infinum.sentinel.data.sources.raw.formatters.Formatter.Companion.VALUES
import com.infinum.sentinel.domain.collectors.Collectors
import com.infinum.sentinel.domain.formatters.Formatters
import com.infinum.sentinel.extensions.sanitize
import org.json.JSONArray
import org.json.JSONObject

internal class JsonFormatter(
    private val context: Context,
    private val applicationCollector: Collectors.Application,
    private val permissionsCollector: Collectors.Permissions,
    private val deviceCollector: Collectors.Device,
    private val preferencesCollector: Collectors.Preferences
) : Formatters.Json {

    companion object {
        private const val INDENT_SPACES = 4
    }

    override fun invoke(): String =
        JSONObject()
            .put(APPLICATION, application())
            .put(PERMISSIONS, permissions())
            .put(DEVICE, device())
            .put(PREFERENCES, preferences())
            .toString(INDENT_SPACES)

    override fun application(): JSONObject =
        JSONObject().apply {
            applicationCollector().let {
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
        }

    override fun permissions(): JSONArray =
        JSONArray().apply {
            permissionsCollector().let {
                it.forEach { entry ->
                    put(
                        JSONObject().apply {
                            addKey(NAME, entry.key)
                            addKey(STATUS, entry.value.toString())
                        }
                    )
                }
            }
        }

    override fun device(): JSONObject =
        JSONObject().apply {
            deviceCollector().let {
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
                addKey(R.string.sentinel_auto_time, it.autoTime)
                addKey(R.string.sentinel_auto_timezone, it.autoTimezone)
            }
        }

    override fun preferences(): JSONArray =
        JSONArray().apply {
            preferencesCollector().let {
                it.forEach { preference ->
                    put(
                        JSONObject().apply {
                            addKey(NAME, preference.name.sanitize())
                            addKey(
                                VALUES,
                                JSONArray().apply {
                                    preference.values.forEach {
                                        put(
                                            JSONObject().apply {
                                                addKey(it.second, it.third)
                                            }
                                        )
                                    }
                                }
                            )
                        }
                    )
                }
            }
        }

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

    private fun JSONObject.addKey(key: String, values: JSONArray) {
        put(key, values)
    }

    private fun JSONObject.addKey(key: String, value: Any?) {
        put(key.sanitize(), value)
    }
}
