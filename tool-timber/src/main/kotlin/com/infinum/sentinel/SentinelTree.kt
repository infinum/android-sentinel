package com.infinum.sentinel

import com.infinum.sentinel.ui.logger.models.BaseEntry
import com.infinum.sentinel.ui.logger.models.FlowBuffer
import com.infinum.sentinel.ui.logger.models.Level
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

internal class SentinelTree(
    val buffer: FlowBuffer<Entry>
) : Timber.DebugTree() {

    override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
        MainScope().launch {
            withContext(Dispatchers.IO) {
                buffer.enqueue(
                    Entry(
                        Level.forLogLevel(priority),
                        System.currentTimeMillis(),
                        tag,
                        message,
                        t?.stackTraceToString()
                    )
                )
            }
        }
    }

    data class Entry(
        override val level: Level,
        override val timestamp: Long,
        override val tag: String? = null,
        override val message: String? = null,
        override val stackTrace: String? = null
    ) : BaseEntry(level, timestamp, tag, message, stackTrace)
}
