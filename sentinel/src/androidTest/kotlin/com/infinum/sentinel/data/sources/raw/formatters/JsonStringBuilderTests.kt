package com.infinum.sentinel.data.sources.raw.formatters

import android.content.Context
import android.os.Build
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.infinum.sentinel.data.sources.raw.collectors.ApplicationCollector
import com.infinum.sentinel.data.sources.raw.collectors.DeviceCollector
import com.infinum.sentinel.data.sources.raw.collectors.PermissionsCollector
import com.infinum.sentinel.data.sources.raw.collectors.PreferencesCollector
import com.infinum.sentinel.domain.collectors.Collectors
import com.infinum.sentinel.ui.SentinelTestApplication
import java.io.File
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.util.ReflectionHelpers

@RunWith(AndroidJUnit4::class)
internal class JsonStringBuilderTests {

    companion object {

        private const val FIELD_MANUFACTURER = "Google"
        private const val FIELD_MODEL = "Android SDK built for x86"
        private const val FIELD_ID = "QSR1.190920.001"
        private const val FIELD_BOOTLOADER = "unknown"
        private const val FIELD_DEVICE = "generic_x86"
        private const val FIELD_BOARD = "goldfish_x86"
        private val FIELD_ARCHITECTURES = arrayOf("x86")
        private const val FIELD_CODENAME = "REL"
        private const val FIELD_RELEASE = "10"
        private const val FIELD_SDK = 29
        private const val FIELD_SECURITY_PATCH = "2019-09-05"

        private const val KEY_BOOLEAN = "my_boolean"
        private const val KEY_FLOAT = "my_float"
        private const val KEY_INT = "my_int"
        private const val KEY_LONG = "my_long"
        private const val KEY_STRING = "my_string"
        private const val KEY_STRING_SET = "my_string_set"

        private const val VALUE_BOOLEAN = true
        private const val VALUE_FLOAT = 1.1f
        private const val VALUE_INT = 7
        private const val VALUE_LONG = 1984L
        private const val VALUE_STRING = "Infinum"
        private val VALUE_STRING_SET = setOf("Bojan", "likes", "Infinum")

        lateinit var context: Context
        lateinit var applicationCollector: Collectors.Application
        lateinit var permissionsCollector: Collectors.Permissions
        lateinit var deviceCollector: Collectors.Device
        lateinit var preferencesCollector: Collectors.Preferences

        @BeforeClass
        @JvmStatic
        fun setupCollector() {
            ReflectionHelpers.setStaticField(
                Build::class.java,
                "MANUFACTURER",
                FIELD_MANUFACTURER
            )
            ReflectionHelpers.setStaticField(Build::class.java, "MODEL", FIELD_MODEL)
            ReflectionHelpers.setStaticField(Build::class.java, "ID", FIELD_ID)
            ReflectionHelpers.setStaticField(
                Build::class.java,
                "BOOTLOADER",
                FIELD_BOOTLOADER
            )
            ReflectionHelpers.setStaticField(Build::class.java, "DEVICE", FIELD_DEVICE)
            ReflectionHelpers.setStaticField(Build::class.java, "BOARD", FIELD_BOARD)
            ReflectionHelpers.setStaticField(
                Build::class.java,
                "SUPPORTED_ABIS",
                FIELD_ARCHITECTURES
            )
            ReflectionHelpers.setStaticField(
                Build.VERSION::class.java,
                "CODENAME",
                FIELD_CODENAME
            )
            ReflectionHelpers.setStaticField(
                Build.VERSION::class.java,
                "RELEASE",
                FIELD_RELEASE
            )
            ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", FIELD_SDK)
            ReflectionHelpers.setStaticField(
                Build.VERSION::class.java,
                "SECURITY_PATCH",
                FIELD_SECURITY_PATCH
            )

            context = ApplicationProvider.getApplicationContext<SentinelTestApplication>()
                .applicationContext
            applicationCollector = ApplicationCollector(context)
            permissionsCollector = PermissionsCollector(context)
            deviceCollector = DeviceCollector(context)
            preferencesCollector = PreferencesCollector(context)
        }
    }

    private val EXPECTED_DATA_NO_PREFERENCES: String = this.javaClass
        .classLoader
        ?.getResourceAsStream("expected_json_no_preferences.json")
        ?.bufferedReader()
        ?.use { it.readText() }
        .orEmpty()
        .replace(Regex("\\s"), "")

    private val EXPECTED_DATA: String = this.javaClass
        .classLoader
        ?.getResourceAsStream("expected_json.json")
        ?.bufferedReader()
        ?.use { it.readText() }
        .orEmpty()
        .replace(Regex("\\s"), "")

    @Before
    fun preferences_deleteDir() {
        val prefsDirectory =
            File(context.applicationInfo.dataDir, PreferencesCollector.PREFS_DIRECTORY)
        val success = (prefsDirectory.exists() && prefsDirectory.isDirectory).let {
            prefsDirectory.deleteRecursively()
        }

        assertTrue(success)
    }

    private fun checkDeviceSpecificFields(json: JSONObject): JSONObject {
        val device = json.optJSONObject("device") ?: throw AssertionError("Device object is missing")

        val fields = listOf(
            "screen_width",
            "screen_height",
            "screen_size",
            "screen_density",
            "font_scale"
        )

        fields.forEach { field ->
            if (!device.has(field)) {
                throw AssertionError("Field $field is missing in the device object")
            }
            device.put(field, "")
        }

        return json
    }

    @Test
    @SmallTest
    fun formatter_hasDataWithoutPreferences() {
        val stringBuilder = JsonFormatter(context, applicationCollector, permissionsCollector, deviceCollector, preferencesCollector)

        val actualData = stringBuilder()
            .replace(Regex("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"), "yyyy-MM-dd HH:mm:ss")
            .replace(Regex("\\s"), "")

        assertNotNull(actualData)
        assertTrue(actualData.isNotBlank())
        val cleanedUpData = checkDeviceSpecificFields(JSONObject(actualData))
        assertEquals(EXPECTED_DATA_NO_PREFERENCES, cleanedUpData.toString())
    }

    @Test
    @SmallTest
    fun formatter_hasData() {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(KEY_BOOLEAN, VALUE_BOOLEAN)
            .putFloat(KEY_FLOAT, VALUE_FLOAT)
            .putInt(KEY_INT, VALUE_INT)
            .putLong(KEY_LONG, VALUE_LONG)
            .putString(KEY_STRING, VALUE_STRING)
            .putStringSet(KEY_STRING_SET, VALUE_STRING_SET)
            .commit()

        val stringBuilder = JsonFormatter(context, applicationCollector, permissionsCollector, deviceCollector, preferencesCollector)

        val actualData = stringBuilder()
            .replace(Regex("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"), "yyyy-MM-dd HH:mm:ss")
            .replace(Regex("\\s"), "")

        assertNotNull(actualData)
        assertTrue(actualData.isNotBlank())
        assertTrue(actualData.isNotBlank())
        val cleanedUpData = checkDeviceSpecificFields(JSONObject(actualData))
        assertEquals(EXPECTED_DATA, cleanedUpData.toString())
    }
}
