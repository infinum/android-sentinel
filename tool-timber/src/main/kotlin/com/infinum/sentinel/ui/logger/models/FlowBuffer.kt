package com.infinum.sentinel.ui.logger.models

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

internal class FlowBuffer<T : BaseEntry> {

    private var queue: List<T> = listOf()
    private val flow: MutableStateFlow<List<T>> = MutableStateFlow(queue.reversed())

    suspend fun enqueue(item: T) {
        queue = queue.plus(item)
        flow.emit(queue.reversed())
    }

    fun asFlow(): Flow<List<T>> = flow.asStateFlow()

    suspend fun clear() {
        queue = emptyList()
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
}
