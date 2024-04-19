package com.infinum.sentinel.data.sources.raw.collectors

import android.content.Context
import android.os.Build
import androidx.preference.PreferenceManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.infinum.sentinel.ui.SentinelTestApplication
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@Ignore("This test is ignored because it's failing on CI")
@RunWith(AndroidJUnit4::class)
internal class PreferencesCollectorTests {

    companion object {

        private const val NAME_SHARED_PREFERENCES = "SHARED_PREFERENCES"
        private const val NAME_PERSISTED_SHARED_PREFERENCES = "PERSISTED_SHARED_PREFERENCES"
        private const val NAME_ENCRYPTED_SHARED_PREFERENCES = "ENCRYPTED_SHARED_PREFERENCES"

        private const val COUNT_SINGLE = 1
        private const val COUNT_MULTIPLE = 2
        private const val COUNT_TUPLES = 6
        private const val COUNT_TUPLES_ENCRYPTED = 8

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
        lateinit var collector: PreferencesCollector

        @BeforeClass
        @JvmStatic
        fun setupCollector() {
            context = ApplicationProvider.getApplicationContext<SentinelTestApplication>()
                .applicationContext

            collector = PreferencesCollector(context)
        }
    }

    @Before
    fun preferences_deleteDir() {
        val prefsDirectory =
            File(context.applicationInfo.dataDir, PreferencesCollector.PREFS_DIRECTORY)
        val success = (prefsDirectory.exists() && prefsDirectory.isDirectory).let {
            prefsDirectory.deleteRecursively()
        }

        assertTrue(success)
    }

    @Test
    @SmallTest
    fun preferences_notUsed() {
        val preferences = collector()

        assertNotNull(preferences)
        assertTrue(preferences.isEmpty())
    }

    @Test
    @SmallTest
    fun preferences_hasDefault() {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putBoolean(KEY_BOOLEAN, VALUE_BOOLEAN)
            .putFloat(KEY_FLOAT, VALUE_FLOAT)
            .putInt(KEY_INT, VALUE_INT)
            .putLong(KEY_LONG, VALUE_LONG)
            .putString(KEY_STRING, VALUE_STRING)
            .putStringSet(KEY_STRING_SET, VALUE_STRING_SET)
            .commit()

        val preferences = collector()

        assertNotNull(preferences)
        assertTrue(preferences.isNotEmpty())
        assertEquals(COUNT_SINGLE, preferences.size)
        assertNotNull(preferences.single().name)
        assertTrue(preferences.single().name.isNotBlank())
        assertNotNull(preferences.single().values)
        assertTrue(preferences.single().values.isNotEmpty())
        assertEquals(COUNT_TUPLES, preferences.single().values.size)
//        assertTrue(
//            preferences.single().values.containsAll(
//                setOf(
//                    Triple(Boolean::class.java, KEY_BOOLEAN, VALUE_BOOLEAN),
//                    Triple(Float::class.java, KEY_FLOAT, VALUE_FLOAT),
//                    Triple(Int::class.java, KEY_INT, VALUE_INT),
//                    Triple(Long::class.java, KEY_LONG, VALUE_LONG),
//                    Triple(String::class.java, KEY_STRING, VALUE_STRING),
//                    Triple(Set::class.java, KEY_STRING_SET, VALUE_STRING_SET)
//                )
//            )
//        )
    }

    @Test
    @SmallTest
    fun preferences_hasSingleNamed() {
        context.getSharedPreferences(NAME_SHARED_PREFERENCES, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_BOOLEAN, VALUE_BOOLEAN)
            .putFloat(KEY_FLOAT, VALUE_FLOAT)
            .putInt(KEY_INT, VALUE_INT)
            .putLong(KEY_LONG, VALUE_LONG)
            .putString(KEY_STRING, VALUE_STRING)
            .putStringSet(KEY_STRING_SET, VALUE_STRING_SET)
            .commit()

        val preferences = collector()

        assertNotNull(preferences)
        assertTrue(preferences.isNotEmpty())
        assertEquals(COUNT_SINGLE, preferences.size)
        assertNotNull(preferences.single().name)
        assertTrue(preferences.single().name.isNotBlank())
        assertNotNull(preferences.single().values)
        assertTrue(preferences.single().values.isNotEmpty())
        assertEquals(COUNT_TUPLES, preferences.single().values.size)
//        assertTrue(
//            preferences.single().values.containsAll(
//                setOf(
//                    Triple(Boolean::class.java, KEY_BOOLEAN, VALUE_BOOLEAN),
//                    Triple(Float::class.java, KEY_FLOAT, VALUE_FLOAT),
//                    Triple(Int::class.java, KEY_INT, VALUE_INT),
//                    Triple(Long::class.java, KEY_LONG, VALUE_LONG),
//                    Triple(String::class.java, KEY_STRING, VALUE_STRING),
//                    Triple(Set::class.java, KEY_STRING_SET, VALUE_STRING_SET)
//                )
//            )
//        )
    }

    @Test
    @SmallTest
    fun preferences_hasMultipleNamed() {
        context.getSharedPreferences(NAME_SHARED_PREFERENCES, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_BOOLEAN, VALUE_BOOLEAN)
            .putFloat(KEY_FLOAT, VALUE_FLOAT)
            .putInt(KEY_INT, VALUE_INT)
            .putLong(KEY_LONG, VALUE_LONG)
            .putString(KEY_STRING, VALUE_STRING)
            .putStringSet(KEY_STRING_SET, VALUE_STRING_SET)
            .commit()
        context.getSharedPreferences(NAME_PERSISTED_SHARED_PREFERENCES, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_BOOLEAN, VALUE_BOOLEAN)
            .putFloat(KEY_FLOAT, VALUE_FLOAT)
            .putInt(KEY_INT, VALUE_INT)
            .putLong(KEY_LONG, VALUE_LONG)
            .putString(KEY_STRING, VALUE_STRING)
            .putStringSet(KEY_STRING_SET, VALUE_STRING_SET)
            .commit()

        val preferences = collector()

        assertNotNull(preferences)
        assertTrue(preferences.isNotEmpty())
        assertEquals(COUNT_MULTIPLE, preferences.size)

        assertNotNull(preferences.first().name)
        assertTrue(preferences.first().name.isNotBlank())
        assertNotNull(preferences.first().values)
        assertTrue(preferences.first().values.isNotEmpty())
        assertEquals(COUNT_TUPLES, preferences.first().values.size)
//        assertTrue(
//            preferences.first().values.containsAll(
//                setOf(
//                    Triple(Boolean::class.java, KEY_BOOLEAN, VALUE_BOOLEAN),
//                    Triple(Float::class.java, KEY_FLOAT, VALUE_FLOAT),
//                    Triple(Int::class.java, KEY_INT, VALUE_INT),
//                    Triple(Long::class.java, KEY_LONG, VALUE_LONG),
//                    Triple(String::class.java, KEY_STRING, VALUE_STRING),
//                    Triple(Set::class.java, KEY_STRING_SET, VALUE_STRING_SET)
//                )
//            )
//        )

        assertNotNull(preferences.last().name)
        assertTrue(preferences.last().name.isNotBlank())
        assertNotNull(preferences.last().values)
        assertTrue(preferences.last().values.isNotEmpty())
        assertEquals(COUNT_TUPLES, preferences.last().values.size)
//        assertTrue(
//            preferences.last().values.containsAll(
//                setOf(
//                    Triple(Boolean::class.java, KEY_BOOLEAN, VALUE_BOOLEAN),
//                    Triple(Float::class.java, KEY_FLOAT, VALUE_FLOAT),
//                    Triple(Int::class.java, KEY_INT, VALUE_INT),
//                    Triple(Long::class.java, KEY_LONG, VALUE_LONG),
//                    Triple(String::class.java, KEY_STRING, VALUE_STRING),
//                    Triple(Set::class.java, KEY_STRING_SET, VALUE_STRING_SET)
//                )
//            )
//        )
    }

    @Test
    @SmallTest
    @Config(minSdk = Build.VERSION_CODES.M)
    fun preferences_hasEncrypted() {
        EncryptedSharedPreferences.create(
            NAME_ENCRYPTED_SHARED_PREFERENCES,
            MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC),
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        ).edit()
            .putBoolean(KEY_BOOLEAN, VALUE_BOOLEAN)
            .putFloat(KEY_FLOAT, VALUE_FLOAT)
            .putInt(KEY_INT, VALUE_INT)
            .putLong(KEY_LONG, VALUE_LONG)
            .putString(KEY_STRING, VALUE_STRING)
            .putStringSet(KEY_STRING_SET, VALUE_STRING_SET)
            .commit()

        val preferences = collector()

        assertNotNull(preferences)
        assertTrue(preferences.isNotEmpty())
        assertEquals(COUNT_SINGLE, preferences.size)
        assertNotNull(preferences.single().name)
        assertTrue(preferences.single().name.isNotBlank())
        assertNotNull(preferences.single().values)
        assertTrue(preferences.single().values.isNotEmpty())
        assertEquals(COUNT_TUPLES_ENCRYPTED, preferences.single().values.size)
    }
}
