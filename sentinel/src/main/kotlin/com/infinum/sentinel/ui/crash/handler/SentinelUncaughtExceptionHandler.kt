package com.infinum.sentinel.ui.crash.handler

import android.content.Context
import com.infinum.sentinel.data.models.local.CrashEntity
import com.infinum.sentinel.data.models.local.crash.CrashData
import com.infinum.sentinel.data.sources.local.room.dao.CrashesDao
import com.infinum.sentinel.extensions.applicationName
import com.infinum.sentinel.extensions.asExceptionData
import com.infinum.sentinel.extensions.asThreadData
import com.infinum.sentinel.ui.shared.notification.NotificationFactory
import kotlinx.coroutines.runBlocking

internal class SentinelUncaughtExceptionHandler(
    private val context: Context,
    private val notificationFactory: NotificationFactory,
    private val dao: CrashesDao,
) : SentinelExceptionHandler {

    private var catchUncaughtExceptions: Boolean = false

    private var currentDefaultHandler: Thread.UncaughtExceptionHandler? = Thread.getDefaultUncaughtExceptionHandler()

    override fun uncaughtException(t: Thread, e: Throwable) {
        if (catchUncaughtExceptions) {
            val entity = CrashEntity(
                applicationName = context.applicationName,
                timestamp = System.currentTimeMillis(),
                data = CrashData(
                    thread = t.asThreadData(),
                    exception = e.asExceptionData()
                )
            )
            val id: Long = runBlocking { dao.save(entity) }
            notificationFactory.showCrash(context.applicationName, id, entity)
        }
        currentDefaultHandler?.uncaughtException(t, e)
    }

    override fun start() {
        catchUncaughtExceptions = true
    }

    override fun stop() {
        catchUncaughtExceptions = false
    }

    override fun setExceptionHandler(handler: Thread.UncaughtExceptionHandler?) {
        this.currentDefaultHandler = handler
    }
}
