package com.infinum.sentinel.data.sources.raw.collectors

import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SdkSuppress
import androidx.test.filters.SmallTest
import com.infinum.sentinel.data.models.raw.ApplicationData
import com.infinum.sentinel.ui.SentinelTestApplication
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

@Ignore("This test is ignored because it's failing on CI")
@RunWith(AndroidJUnit4::class)
internal class ApplicationCollectorTests {

    companion object {

        private const val EXPECTED_APPLICATION_LABEL = "Sentinel"
        private const val VERSION_CODE = "100000"
        private const val VERSION_NAME = "1.0.0"
        private const val MIN_SDK = "21"
        private const val PACKAGE_NAME = "com.infinum.sentinel.test"
        private const val PROCESS_NAME = "com.infinum.sentinel.test"
        private const val TASK_AFFINITY = "com.infinum.sentinel.test"
        private const val LOCALE_COUNTRY = "US"
        private const val LOCALE_LANGUAGE = "en"
        private val DATE_TIME_FORMAT = Regex("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}")

        lateinit var actualApplicationData: ApplicationData

        @BeforeClass
        @JvmStatic
        fun setupCollector() {
            val context =
                ApplicationProvider
                    .getApplicationContext<SentinelTestApplication>()
                    .applicationContext

            val collector = ApplicationCollector(context)

            actualApplicationData = collector()
        }
    }

    @Test
    @SmallTest
    fun application_hasIcon() {
        assertNotNull(actualApplicationData.applicationIcon)
    }

    @Test
    @SmallTest
    fun application_hasLabel() {
        assertNotNull(actualApplicationData.applicationName)
        assertEquals(EXPECTED_APPLICATION_LABEL, actualApplicationData.applicationName)
    }

    @Test
    @SmallTest
    fun application_hasNoBlankLabel() {
        Assert.assertNotEquals("", actualApplicationData.applicationName)
        assertEquals(EXPECTED_APPLICATION_LABEL, actualApplicationData.applicationName)
    }

    @Test
    @SmallTest
    fun application_hasVersionCode() {
        assertNotNull(actualApplicationData.versionCode)
        assertEquals(VERSION_CODE, actualApplicationData.versionCode)
    }

    @Test
    @SmallTest
    fun application_hasVersionName() {
        assertNotNull(actualApplicationData.versionName)
        assertEquals(VERSION_NAME, actualApplicationData.versionName)
    }

    @Test
    @SmallTest
    fun application_hasFirstInstall() {
        assertNotNull(actualApplicationData.firstInstall)
        assertTrue(actualApplicationData.firstInstall.isNotBlank())
        assertTrue(actualApplicationData.firstInstall.matches(DATE_TIME_FORMAT))
    }

    @Test
    @SmallTest
    fun application_hasLastUpdate() {
        assertNotNull(actualApplicationData.lastUpdate)
        assertTrue(actualApplicationData.lastUpdate.isNotBlank())
        assertTrue(actualApplicationData.lastUpdate.matches(DATE_TIME_FORMAT))
    }

    @Test
    @SmallTest
    @SdkSuppress(minSdkVersion = Build.VERSION_CODES.O)
    fun application_hasMinSdk_Oreo() {
        assertNotNull(actualApplicationData.minSdk)
        assertTrue(actualApplicationData.minSdk.isNotBlank())
        assertEquals(MIN_SDK, actualApplicationData.minSdk)
    }

    @Test
    @SmallTest
    @SdkSuppress(
        minSdkVersion = Build.VERSION_CODES.JELLY_BEAN,
        maxSdkVersion = Build.VERSION_CODES.O
    )
    fun application_hasMinSdk_Before_Oreo() {
        assertNotNull(actualApplicationData.minSdk)
        assertTrue(actualApplicationData.minSdk.isBlank())
    }

    @Test
    @SmallTest
    fun application_hasTargetSdk() {
        assertNotNull(actualApplicationData.targetSdk)
        assertTrue(actualApplicationData.targetSdk.isNotBlank())
    }

    @Test
    @SmallTest
    fun application_hasPackageName() {
        assertNotNull(actualApplicationData.packageName)
        assertTrue(actualApplicationData.packageName.isNotBlank())
        assertEquals(PACKAGE_NAME, actualApplicationData.packageName)
    }

    @Test
    @SmallTest
    fun application_hasProcessName() {
        assertNotNull(actualApplicationData.processName)
        assertTrue(actualApplicationData.processName.isNotBlank())
        assertEquals(PROCESS_NAME, actualApplicationData.processName)
    }

    @Test
    @SmallTest
    fun application_hasTaskAffinity() {
        assertNotNull(actualApplicationData.taskAffinity)
        assertTrue(actualApplicationData.taskAffinity.isNotBlank())
        assertEquals(TASK_AFFINITY, actualApplicationData.taskAffinity)
    }

    @Test
    @SmallTest
    fun application_hasLocaleCountry() {
        assertNotNull(actualApplicationData.localeCountry)
        assertTrue(actualApplicationData.localeCountry.isNotBlank())
        assertEquals(LOCALE_COUNTRY, actualApplicationData.localeCountry)
    }

    @Test
    @SmallTest
    fun application_hasLocaleLanguage() {
        assertNotNull(actualApplicationData.localeLanguage)
        assertTrue(actualApplicationData.localeLanguage.isNotBlank())
        assertEquals(LOCALE_LANGUAGE, actualApplicationData.localeLanguage)
    }
}
