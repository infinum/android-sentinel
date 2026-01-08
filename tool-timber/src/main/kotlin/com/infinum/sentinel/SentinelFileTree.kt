package com.infinum.sentinel

import android.content.Context
import com.infinum.sentinel.ui.logger.models.BaseEntry
import com.infinum.sentinel.ui.logger.models.FlowBuffer
import com.infinum.sentinel.ui.logger.models.Level
import com.infinum.sentinel.ui.shared.LogFileResolver
import com.infinum.sentinel.ui.shared.TimberToolConstants.LOG_DATE_TIME_FORMAT
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

internal class SentinelFileTree(
    context: Context,
    val buffer: FlowBuffer<Entry>,
) : Timber.DebugTree() {
    private val logFileResolver = LogFileResolver(context)

    override fun log(
        priority: Int,
        tag: String?,
        message: String,
        t: Throwable?,
    ) {
        val dateTimeFormat = SimpleDateFormat(LOG_DATE_TIME_FORMAT, Locale.getDefault())

        MainScope().launch {
            withContext(Dispatchers.IO) {
                val entry =
                    Entry(
                        Level.forLogLevel(priority),
                        System.currentTimeMillis(),
                        tag,
                        message,
                        t?.stackTraceToString(),
                    )

                buffer.enqueue(entry)

                val file: File = logFileResolver.createOrOpenFile()
                val line = entry.asLineString(dateTimeFormat)

                file.appendText(line)
            }
        }
    }

    data class Entry(
        override val level: Level,
        override val timestamp: Long,
        override val tag: String? = null,
        override val message: String? = null,
        override val stackTrace: String? = null,
    ) : BaseEntry(level, timestamp, tag, message, stackTrace)
}
