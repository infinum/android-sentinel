package com.infinum.sentinel.extensions

import android.content.Intent
import android.os.Build
import java.io.Serializable

@Suppress("DEPRECATION")
public inline fun <reified T: Serializable> Intent.getSerializableExtraHelper(extraKey: String): T? {
    return if (Build.VERSION.SDK_INT >= 33) {
        this.getSerializableExtra(extraKey, T::class.java)
    } else {
        this.getSerializableExtra(extraKey) as T?
    }

}