package com.infinum.sentinel.ui.logger.models

import kotlin.coroutines.resume
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONArray
import org.json.JSONObject

internal class FlowBuffer<T : BaseEntry> {

    private val queue: MutableList<T> = mutableListOf()
    private val flow: MutableStateFlow<List<T>> = MutableStateFlow(queue.reversed())

    suspend fun enqueue(item: T) {
        val ok = queue.add(item)
        if (ok) {
            flow.emit(queue.reversed())
        }
    }

    fun asFlow(): Flow<List<T>> = flow.asStateFlow()

    suspend fun clear() {
        queue.clear()
        flow.emit(queue)
    }

    suspend fun filter(query: String?) {
        flow.emit(
            if (query.isNullOrBlank()) {
                queue.reversed()
            } else {
                queue.reversed().filter {
                    it.tag?.lowercase()?.contains(query.lowercase()) == true ||
                            it.message?.lowercase()?.contains(query.lowercase()) == true ||
                            it.stackTrace?.lowercase()?.contains(query.lowercase()) == true
                }
            }
        )
    }

    suspend fun asString(): String =
        suspendCancellableCoroutine {
            val result = JSONArray(
                queue.map { entry -> JSONObject(entry.asString()) }.toTypedArray()
            ).toString()
            it.resume(result)
        }
}
