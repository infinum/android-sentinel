package com.infinum.sentinel.ui.shared

import android.content.Context
import java.io.File
import java.util.Calendar
import java.util.Locale

internal class LogFileResolver(
    private val context: Context
) {

    companion object {
        private const val LOGS_DIRECTORY = "/logs"
        private const val LOG_EXTENSION = ".log"
    }

    private val parent = File("${context.filesDir.absolutePath}${LOGS_DIRECTORY}")

    fun logsDir() = parent

    fun createOrOpenFile(): File {
        if (parent.exists().not()) {
            parent.mkdirs()
        }

        val day = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        val month = Calendar.getInstance().get(Calendar.MONTH) + 1
        val year = Calendar.getInstance().get(Calendar.YEAR)

        val filename = buildString {
            val locale = Locale.getDefault()
            append(String.format(locale = locale, format = "%02d", day))
            append("-")
            append(String.format(locale = locale, format = "%02d", month))
            append("-")
            append(String.format(locale = locale, format = "%04d", year))
            append(LOG_EXTENSION)
        }
        val nowFile = File("${parent.absolutePath}/$filename")

        if (nowFile.exists().not()) {
            nowFile.createNewFile()
        }

        return nowFile
    }
}
