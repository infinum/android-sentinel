package com.infinum.sentinel.ui.crash.notification

import com.infinum.sentinel.data.models.local.CrashEntity

internal interface NotificationFactory {

    fun showCrash(applicationName: String, id: Long, entity: CrashEntity)
}
