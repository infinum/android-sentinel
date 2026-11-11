package com.infinum.sentinel.domain.networkproxy

import okio.Buffer
import okio.ForwardingSource
import okio.Source

internal class CapturingSource(
    delegate: Source,
    private val limit: Long,
    private val onFinished: (Buffer?) -> Unit,
) : ForwardingSource(delegate) {
    private val captured = Buffer()
    private var overflowed = false
    private var finished = false

    override fun read(
        sink: Buffer,
        byteCount: Long,
    ): Long {
        val read = super.read(sink, byteCount)
        when {
            read == -1L -> finish(readToEnd = true)
            overflowed -> Unit
            captured.size + read > limit -> {
                overflowed = true
                captured.clear()
            }
            else -> sink.copyTo(captured, sink.size - read, read)
        }
        return read
    }

    override fun close() {
        super.close()
        finish(readToEnd = false)
    }

    private fun finish(readToEnd: Boolean) {
        if (finished) return
        finished = true
        onFinished(captured.takeIf { readToEnd && !overflowed })
    }
}
