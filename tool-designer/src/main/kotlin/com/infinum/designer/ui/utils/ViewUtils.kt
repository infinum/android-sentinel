package com.infinum.designer.ui.utils

import android.os.Build
import android.view.WindowManager


object ViewUtils {

    fun getWindowType(): Int =
        getWindowType(false)

    @Suppress("DEPRECATION")
    fun getWindowType(useSystemAlert: Boolean): Int =
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
            if (useSystemAlert) {
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT
            } else {
                WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY
            }
        } else {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        }
}