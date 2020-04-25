package com.infinum.sentinel.data.sources.raw

import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.infinum.sentinel.data.models.raw.DeviceData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.util.ReflectionHelpers

@RunWith(AndroidJUnit4::class)
internal class DeviceCollectorTests {

    companion object {

        private const val DEVICE_FIELD_MANUFACTURER = "Samsung"
        private const val DEVICE_FIELD_MODEL = "SM-G965F"
        private const val DEVICE_FIELD_ID = "QP1A.190711.020"
        private const val DEVICE_FIELD_BOOTLOADER = "G965FXXU8DTC5"
        private const val DEVICE_FIELD_DEVICE = "star2lte"
        private const val DEVICE_FIELD_BOARD = "exynos9810"
        private val DEVICE_FIELD_ARCHITECTURES = arrayOf(
            "arm64-v8a",
            "armeabi-v7a",
            "armeabi"
        )
        private const val DEVICE_FIELD_CODENAME = "REL"
        private const val DEVICE_FIELD_RELEASE = "10"
        private const val DEVICE_FIELD_SDK = 29
        private const val DEVICE_FIELD_SECURITY_PATCH = "2020-04-01"

        lateinit var actualDeviceData: DeviceData

        @BeforeClass
        @JvmStatic
        fun setupCollector() {
            ReflectionHelpers.setStaticField(
                Build::class.java,
                "MANUFACTURER",
                DEVICE_FIELD_MANUFACTURER
            )
            ReflectionHelpers.setStaticField(Build::class.java, "MODEL", DEVICE_FIELD_MODEL)
            ReflectionHelpers.setStaticField(Build::class.java, "ID", DEVICE_FIELD_ID)
            ReflectionHelpers.setStaticField(
                Build::class.java,
                "BOOTLOADER",
                DEVICE_FIELD_BOOTLOADER
            )
            ReflectionHelpers.setStaticField(Build::class.java, "DEVICE", DEVICE_FIELD_DEVICE)
            ReflectionHelpers.setStaticField(Build::class.java, "BOARD", DEVICE_FIELD_BOARD)
            ReflectionHelpers.setStaticField(
                Build::class.java,
                "SUPPORTED_ABIS",
                DEVICE_FIELD_ARCHITECTURES
            )
            ReflectionHelpers.setStaticField(
                Build.VERSION::class.java,
                "CODENAME",
                DEVICE_FIELD_CODENAME
            )
            ReflectionHelpers.setStaticField(
                Build.VERSION::class.java,
                "RELEASE",
                DEVICE_FIELD_RELEASE
            )
            ReflectionHelpers.setStaticField(Build.VERSION::class.java, "SDK_INT", DEVICE_FIELD_SDK)
            ReflectionHelpers.setStaticField(
                Build.VERSION::class.java,
                "SECURITY_PATCH",
                DEVICE_FIELD_SECURITY_PATCH
            )

            val collector = DeviceCollector()
            collector.collect()

            actualDeviceData = collector.present()
        }
    }

    @Test
    @SmallTest
    fun device_hasManufacturer() {
        assertEquals(actualDeviceData.manufacturer, DEVICE_FIELD_MANUFACTURER)
    }

    @Test
    @SmallTest
    fun device_hasModel() {
        assertEquals(actualDeviceData.model, DEVICE_FIELD_MODEL)
    }

    @Test
    @SmallTest
    fun device_hasId() {
        assertEquals(actualDeviceData.id, DEVICE_FIELD_ID)
    }

    @Test
    @SmallTest
    fun device_hasBootloader() {
        assertEquals(actualDeviceData.bootloader, DEVICE_FIELD_BOOTLOADER)
    }

    @Test
    @SmallTest
    fun device_hasDevice() {
        assertEquals(actualDeviceData.device, DEVICE_FIELD_DEVICE)
    }

    @Test
    @SmallTest
    fun device_hasBoard() {
        assertEquals(actualDeviceData.board, DEVICE_FIELD_BOARD)
    }

    @Test
    @SmallTest
    fun device_hasArchitectures() {
        assertEquals(actualDeviceData.architectures, DEVICE_FIELD_ARCHITECTURES.joinToString())
    }

    @Test
    @SmallTest
    fun device_hasCodename() {
        assertEquals(actualDeviceData.codename, DEVICE_FIELD_CODENAME)
    }

    @Test
    @SmallTest
    fun device_hasRelease() {
        assertEquals(actualDeviceData.release, DEVICE_FIELD_RELEASE)
    }

    @Test
    @SmallTest
    fun device_hasSdkInt() {
        assertEquals(actualDeviceData.sdk, DEVICE_FIELD_SDK.toString())
    }

    @Test
    @SmallTest
    fun device_hasSecurityPatch() {
        assertEquals(actualDeviceData.securityPatch, DEVICE_FIELD_SECURITY_PATCH)
    }

    @Test
    @SmallTest
    fun device_isNotEmulator() {
        assertFalse(actualDeviceData.isProbablyAnEmulator)
    }
}
