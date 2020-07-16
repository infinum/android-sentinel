package com.infinum.designer.ui.utils

import android.content.Intent

/**
 * This class is a dirty little hack to transfer Intent object from Activity to Service.
 */
object MediaProjectionHelper {
    var data: Intent? = null
}