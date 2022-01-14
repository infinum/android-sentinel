package com.infinum.sentinel.ui.shared.notification

import com.infinum.sentinel.data.models.local.CrashEntity

internal interface NotificationFactory {

    fun showCrash(applicationName: String, id: Long, entity: CrashEntity)

    fun showAnr(applicationName: String, id: Long, entity: CrashEntity)

    fun showExpiredCertificate(applicationName: String, count: Int)

    fun showToExpireCertificate(applicationName: String, count: Int)
}
