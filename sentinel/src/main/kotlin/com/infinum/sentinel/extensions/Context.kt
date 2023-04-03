package com.infinum.sentinel.extensions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import android.widget.Toast
import com.infinum.sentinel.R
import java.math.RoundingMode
import kotlin.math.pow
import kotlin.math.sqrt

private const val KEY_TRIGGER_SHAKE: String =
    "com.infinum.sentinel.trigger.shake"

private const val KEY_TRIGGER_PROXIMITY: String =
    "com.infinum.sentinel.trigger.proximity"

private const val KEY_TRIGGER_FOREGROUND: String =
    "com.infinum.sentinel.trigger.foreground"

private const val KEY_TRIGGER_USB_CONNECTED: String =
    "com.infinum.sentinel.trigger.usb_connected"

private const val KEY_TRIGGER_AIRPLANE_MODE_ON: String =
    "com.infinum.sentinel.trigger.airplane_mode_on"

internal fun Context.isPermissionGranted(name: String): Boolean =
    packageManager.checkPermission(name, packageName) == PackageManager.PERMISSION_GRANTED

internal fun Context.copyToClipboard(key: String, value: String): Boolean =
    (getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager)
        ?.let {
            it.setPrimaryClip(ClipData.newPlainText(key, value))
            Toast.makeText(
                this,
                R.string.sentinel_text_copied,
                Toast.LENGTH_SHORT
            ).show()
            true
        } ?: run {
        Toast.makeText(
            this,
            R.string.sentinel_failed_to_copy_to_clipboard,
            Toast.LENGTH_SHORT
        ).show()
        false
    }

@Suppress("DEPRECATION")
private val Context.info: ApplicationInfo
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        packageManager
            .getApplicationInfo(
                packageName,
                PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong())
            )
    } else {
        packageManager
            .getApplicationInfo(
                packageName,
                PackageManager.GET_META_DATA
            )
    }

private fun Context.enableTrigger(key: String): Boolean? =
    info.metaData
        ?.getInt(key, -1)
        ?.takeIf { it != -1 }
        ?.let {
            when (it) {
                1 -> true
                0 -> false
                else -> null
            }
        }

internal fun Context.enableShakeTrigger(): Boolean? =
    enableTrigger(KEY_TRIGGER_SHAKE)

internal fun Context.enableProximityTrigger(): Boolean? =
    enableTrigger(KEY_TRIGGER_PROXIMITY)

internal fun Context.enableForegroundTrigger(): Boolean? =
    enableTrigger(KEY_TRIGGER_FOREGROUND)

internal fun Context.enableUsbConnectedTrigger(): Boolean? =
    enableTrigger(KEY_TRIGGER_USB_CONNECTED)

internal fun Context.enableAirplaneModeOnTrigger(): Boolean? =
    enableTrigger(KEY_TRIGGER_AIRPLANE_MODE_ON)

internal val Context.applicationName: String
    get() = (packageManager.getApplicationLabel(this.info) as? String)
        ?: getString(R.string.sentinel_name)

@Suppress("DEPRECATION")
internal val Context.widthPixels: Int
    get() {
        val metrics = DisplayMetrics()
        val manager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            manager.currentWindowMetrics.bounds.width()
        } else {
            manager.defaultDisplay.getRealMetrics(metrics)
            metrics.widthPixels
        }
    }

@Suppress("DEPRECATION")
internal val Context.heightPixels: Int
    get() {
        val metrics = DisplayMetrics()
        val manager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            manager.currentWindowMetrics.bounds.height()
        } else {
            manager.defaultDisplay.getRealMetrics(metrics)
            metrics.heightPixels
        }
    }

internal val Context.xdpi
    get() = resources.displayMetrics.xdpi

internal val Context.ydpi
    get() = resources.displayMetrics.ydpi

internal val Context.screenSize
    get() = sqrt(
        (widthPixels / xdpi.toDouble()).pow(2.0) + (heightPixels / ydpi.toDouble()).pow(2.0)
    ).toBigDecimal().setScale(1, RoundingMode.HALF_EVEN).toDouble()

internal val Context.density
    get() = resources.configuration.densityDpi

internal val Context.fontScale
    get() = resources.configuration.fontScale

internal val Context.installerPackage: String?
    get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        packageManager.getInstallSourceInfo(packageName).installingPackageName
    } else {
        @Suppress("DEPRECATION")
        packageManager.getInstallerPackageName(packageName)
    }
