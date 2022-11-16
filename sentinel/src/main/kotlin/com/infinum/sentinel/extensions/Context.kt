package com.infinum.sentinel.extensions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.DisplayMetrics
import android.view.WindowManager
import android.widget.Toast
import com.infinum.sentinel.R
import java.math.RoundingMode
import kotlin.math.pow
import kotlin.math.sqrt

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

internal val Context.applicationName: String
    get() = (
        packageManager.getApplicationLabel(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getApplicationInfo(
                    packageName,
                    PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getApplicationInfo(
                    packageName,
                    PackageManager.GET_META_DATA
                )
            }
        ) as? String
        ) ?: getString(R.string.sentinel_name)

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
