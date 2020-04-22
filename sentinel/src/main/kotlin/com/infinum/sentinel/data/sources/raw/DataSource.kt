package com.infinum.sentinel.data.sources.raw

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.os.Build
import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.data.models.raw.AppInfo
import com.infinum.sentinel.data.models.raw.DeviceInfo

@SuppressLint("DefaultLocale")
internal object DataSource {

    lateinit var applicationIcon: Drawable

    var applicationData: Map<AppInfo, String> = mapOf()

    var permissions: Map<String, Boolean> = mapOf()

    val deviceData: Map<DeviceInfo, String> = mapOf(
        DeviceInfo.MANUFACTURER to Build.MANUFACTURER.toLowerCase().capitalize(),
        DeviceInfo.MODEL to Build.MODEL,
        DeviceInfo.ID to Build.ID,
        DeviceInfo.BOOTLOADER to Build.BOOTLOADER,
        DeviceInfo.DEVICE to Build.DEVICE,
        DeviceInfo.BOARD to Build.BOARD,
        DeviceInfo.ARCHITECTURES to if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Build.SUPPORTED_ABIS.joinToString()
        } else "",
        DeviceInfo.CODENAME to Build.VERSION.CODENAME,
        DeviceInfo.RELEASE to Build.VERSION.RELEASE,
        DeviceInfo.SDK to Build.VERSION.SDK_INT.toString(),
        DeviceInfo.SECURITY_PATCH to if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Build.VERSION.SECURITY_PATCH
        } else ""
    ).filter { it.value.isNullOrBlank().not() }

    var toolsData: List<Sentinel.Tool> = listOf()
}
