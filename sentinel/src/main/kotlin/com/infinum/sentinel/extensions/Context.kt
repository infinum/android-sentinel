package com.infinum.sentinel.extensions

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import com.infinum.sentinel.R

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
