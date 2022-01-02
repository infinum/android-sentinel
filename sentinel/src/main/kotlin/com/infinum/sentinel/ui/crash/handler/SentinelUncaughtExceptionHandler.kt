package com.infinum.sentinel.ui.crash.handler

import android.content.Context
import android.content.pm.PackageManager
import com.infinum.sentinel.R
import com.infinum.sentinel.data.models.local.CrashEntity
import com.infinum.sentinel.data.models.local.crash.CrashData
import com.infinum.sentinel.data.sources.local.room.dao.CrashesDao
import com.infinum.sentinel.extensions.asExceptionData
import com.infinum.sentinel.extensions.asThreadData
import com.infinum.sentinel.ui.crash.notification.NotificationFactory
import kotlinx.coroutines.runBlocking

internal class SentinelUncaughtExceptionHandler(
    context: Context,
    private val notificationFactory: NotificationFactory,
    private val dao: CrashesDao,
) : SentinelExceptionHandler {

    private var catchUncaughtExceptions: Boolean = true

    private val applicationName: String = (context.packageManager.getApplicationLabel(
        context.packageManager.getApplicationInfo(context.packageName, PackageManager.GET_META_DATA)
    ) as? String) ?: context.getString(R.string.sentinel_name)

    private var currentDefaultHandler: Thread.UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(t: Thread, e: Throwable) {
        if (catchUncaughtExceptions) {
            val entity = CrashEntity(
                applicationName = applicationName,
                timestamp = System.currentTimeMillis(),
                data = CrashData(
                    thread = t.asThreadData(),
                    exception = e.asExceptionData()
                )
            )
            val id: Long = runBlocking { dao.save(entity) }
            notificationFactory.showCrash(applicationName, id, entity)
        }
        currentDefaultHandler?.uncaughtException(t, e)
    }

    override fun startCatchingUncaughtExceptions() {
        catchUncaughtExceptions = true
    }

    override fun stopCatchingUncaughtExceptions() {
        catchUncaughtExceptions = false
    }

    override fun setExceptionHandler(handler: Thread.UncaughtExceptionHandler?) {
        this.currentDefaultHandler = handler
    }
}
