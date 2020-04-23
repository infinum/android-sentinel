package com.infinum.sentinel.data.models.raw

import android.annotation.SuppressLint
import android.os.Build

@SuppressLint("DefaultLocale")
internal data class DeviceData(
    val manufacturer: String = Build.MANUFACTURER.toLowerCase().capitalize(),
    val model: String = Build.MODEL,
    val id: String = Build.ID,
    val bootloader: String = Build.BOOTLOADER,
    val device: String = Build.DEVICE,
    val board: String = Build.BOARD,
    val architectures: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        Build.SUPPORTED_ABIS.joinToString()
    } else "",
    val codename: String = Build.VERSION.CODENAME,
    val release: String = Build.VERSION.RELEASE,
    val sdk: String = Build.VERSION.SDK_INT.toString(),
    val securityPatch: String = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        Build.VERSION.SECURITY_PATCH
    } else ""
)
