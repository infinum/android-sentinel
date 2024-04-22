package com.infinum.sentinel.data.sources.raw.collectors

import android.content.Context
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.infinum.sentinel.data.models.raw.DeviceData
import com.infinum.sentinel.ui.SentinelTestApplication
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.util.ReflectionHelpers

@RunWith(AndroidJUnit4::class)
internal class DeviceCollectorDeviceTests {

    companion object {

        private const val FIELD_MANUFACTURER = "Samsung"
        private const val FIELD_MODEL = "SM-G965F"
        private const val FIELD_ID = "QP1A.190711.020"
        private const val FIELD_BOOTLOADER = "G965FXXU8DTC5"
        private const val FIELD_DEVICE = "star2lte"
        private const val FIELD_BOARD = "exynos9810"
        private val FIELD_ARCHITECTURES = arrayOf("arm64-v8a", "armeabi-v7a", "armeabi")
        private const val FIELD_CODENAME = "REL"
        private const val FIELD_RELEASE = "10"
        private const val FIELD_SDK = 29
        private const val FIELD_SECURITY_PATCH = "2020-04-01"

        lateinit var actualDeviceData: DeviceData

        lateinit var context: Context

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

            context = ApplicationProvider.getApplicationContext<SentinelTestApplication>().applicationContext

            val collector = DeviceCollector(context)

            actualDeviceData = collector()
        }
    }

    @Test
    @SmallTest
    fun device_hasManufacturer() {
        assertEquals(actualDeviceData.manufacturer, FIELD_MANUFACTURER)
    }

    @Test
    @SmallTest
    fun device_hasModel() {
        assertEquals(actualDeviceData.model, FIELD_MODEL)
    }

    @Test
    @SmallTest
    fun device_hasId() {
        assertEquals(actualDeviceData.id, FIELD_ID)
    }

    @Test
    @SmallTest
    fun device_hasBootloader() {
        assertEquals(actualDeviceData.bootloader, FIELD_BOOTLOADER)
    }

    @Test
    @SmallTest
    fun device_hasDevice() {
        assertEquals(actualDeviceData.device, FIELD_DEVICE)
    }

    @Test
    @SmallTest
    fun device_hasBoard() {
        assertEquals(actualDeviceData.board, FIELD_BOARD)
    }

    @Test
    @SmallTest
    fun device_hasArchitectures() {
        assertEquals(actualDeviceData.architectures, FIELD_ARCHITECTURES.joinToString())
    }

    @Test
    @SmallTest
    fun device_hasCodename() {
        assertEquals(actualDeviceData.codename, FIELD_CODENAME)
    }

    @Test
    @SmallTest
    fun device_hasRelease() {
        assertEquals(actualDeviceData.release, FIELD_RELEASE)
    }

    @Test
    @SmallTest
    fun device_hasSdkInt() {
        assertEquals(actualDeviceData.sdk, FIELD_SDK.toString())
    }

    @Test
    @SmallTest
    fun device_hasSecurityPatch() {
        assertEquals(actualDeviceData.securityPatch, FIELD_SECURITY_PATCH)
    }

    @Test
    @SmallTest
    fun device_isNotEmulator() {
        assertFalse(actualDeviceData.isProbablyAnEmulator)
    }
}
