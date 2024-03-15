package com.infinum.sentinel.data.sources.raw.formatters

import android.content.Context
import android.os.Build
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.infinum.sentinel.data.sources.raw.collectors.PreferencesCollector
import com.infinum.sentinel.ui.SentinelTestApplication
import java.io.File
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.util.ReflectionHelpers

@RunWith(AndroidJUnit4::class)
internal class PlainStringBuilderTests {
//
//    companion object {
//
//        private const val FIELD_MANUFACTURER = "Google"
//        private const val FIELD_MODEL = "Android SDK built for x86"
//        private const val FIELD_ID = "QSR1.190920.001"
//        private const val FIELD_BOOTLOADER = "unknown"
//        private const val FIELD_DEVICE = "generic_x86"
//        private const val FIELD_BOARD = "goldfish_x86"
//        private val FIELD_ARCHITECTURES = arrayOf("x86")
//        private const val FIELD_CODENAME = "REL"
//        private const val FIELD_RELEASE = "10"
//        private const val FIELD_SDK = 29
//        private const val FIELD_SECURITY_PATCH = "2019-09-05"
//
//        private const val KEY_BOOLEAN = "my_boolean"
//        private const val KEY_FLOAT = "my_float"
//        private const val KEY_INT = "my_int"
//        private const val KEY_LONG = "my_long"
//        private const val KEY_STRING = "my_string"
//        private const val KEY_STRING_SET = "my_string_set"
//
//        private const val VALUE_BOOLEAN = true
//        private const val VALUE_FLOAT = 1.1f
//        private const val VALUE_INT = 7
//        private const val VALUE_LONG = 1984L
//        private const val VALUE_STRING = "Infinum"
//        private val VALUE_STRING_SET = setOf("Bojan", "likes", "Infinum")
//
//        lateinit var context: Context
//
//        @BeforeClass
//        @JvmStatic
//        fun setupCollector() {
//            ReflectionHelpers.setStaticField(
//                Build::class.java,
//                "MANUFACTURER",
//                FIELD_MANUFACTURER
//            )
//            ReflectionHelpers.setStaticField(Build::class.java, "MODEL", FIELD_MODEL)
//            ReflectionHelpers.setStaticField(Build::class.java, "ID", FIELD_ID)
//            ReflectionHelpers.setStaticField(
//                Build::class.java,
//                "BOOTLOADER",
//                FIELD_BOOTLOADER
//            )
//            ReflectionHelpers.setStaticField(Build::class.java, "DEVICE", FIELD_DEVICE)
//            ReflectionHelpers.setStaticField(Build::class.java, "BOARD", FIELD_BOARD)
//            ReflectionHelpers.setStaticField(
//                Build::class.java,
//                "SUPPORTED_ABIS",
//                FIELD_ARCHITECTURES
//            )
//            ReflectionHelpers.setStaticField(
//                Build.VERSION::class.java,
//                "CODENAME",
//                FIELD_CODENAME
//            )
//            ReflectionHelpers.setStaticField(
//                Build.VERSION::class.java,
//                "RELEASE",
//                FIELD_RELEASE
//            )
//            ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", FIELD_SDK)
//            ReflectionHelpers.setStaticField(
//                Build.VERSION::class.java,
//                "SECURITY_PATCH",
//                FIELD_SECURITY_PATCH
//            )
//
//            context = ApplicationProvider.getApplicationContext<SentinelTestApplication>()
//                .applicationContext
//        }
//    }
//
//    private val EXPECTED_DATA_NO_PREFERENCES: String = this.javaClass
//        .classLoader
//        ?.getResourceAsStream("expected_plain_no_preferences.txt")
//        ?.bufferedReader()
//        ?.use { it.readText() }
//        .orEmpty()
//
//    private val EXPECTED_DATA: String = this.javaClass
//        .classLoader
//        ?.getResourceAsStream("expected_plain.txt")
//        ?.bufferedReader()
//        ?.use { it.readText() }
//        .orEmpty()
//
//    @Before
//    fun preferences_deleteDir_Before() {
//        val prefsDirectory =
//            File(context.applicationInfo.dataDir, PreferencesCollector.PREFS_DIRECTORY)
//        val success = (prefsDirectory.exists() && prefsDirectory.isDirectory).let {
//            prefsDirectory.deleteRecursively()
//        }
//
//        assertTrue(success)
//    }
//
//    @After
//    fun preferences_deleteDir_After() {
//        val prefsDirectory =
//            File(context.applicationInfo.dataDir, PreferencesCollector.PREFS_DIRECTORY)
//        val success = (prefsDirectory.exists() && prefsDirectory.isDirectory).let {
//            prefsDirectory.deleteRecursively()
//        }
//
//        assertTrue(success)
//    }
//
//    @Test
//    @SmallTest
//    fun formatter_hasDataWithoutPreferences() {
//        val stringBuilder = PlainFormatter(context)
//
//        val actualData = stringBuilder()
//            .replace(Regex("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"), "yyyy-MM-dd HH:mm:ss")
//        assertNotNull(actualData)
//        assertTrue(actualData.isNotBlank())
//        assertEquals(EXPECTED_DATA_NO_PREFERENCES, actualData)
//    }
//
//    @Test
//    @SmallTest
//    fun formatter_hasData() {
//        PreferenceManager.getDefaultSharedPreferences(context)
//            .edit()
//            .putBoolean(KEY_BOOLEAN, VALUE_BOOLEAN)
//            .putFloat(KEY_FLOAT, VALUE_FLOAT)
//            .putInt(KEY_INT, VALUE_INT)
//            .putLong(KEY_LONG, VALUE_LONG)
//            .putString(KEY_STRING, VALUE_STRING)
//            .putStringSet(KEY_STRING_SET, VALUE_STRING_SET)
//            .commit()
//
//        val stringBuilder = PlainFormatter(context)
//
//        val actualData = stringBuilder()
//            .replace(Regex("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"), "yyyy-MM-dd HH:mm:ss")
//
//        assertNotNull(actualData)
//        assertTrue(actualData.isNotBlank())
//        assertEquals(EXPECTED_DATA, actualData)
//    }
}
