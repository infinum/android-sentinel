package com.infinum.sentinel.domain.networkproxy

import com.infinum.sentinel.domain.networkproxy.models.HttpMethod
import com.infinum.sentinel.domain.networkproxy.models.RecentCall
import java.util.concurrent.CopyOnWriteArraySet

internal class RecentCallStore(private val capacity: Int = DEFAULT_CAPACITY) {
    fun interface Listener {
        fun onRecentCallsChanged()
    }

    private val lock = Any()
    private val calls = ArrayDeque<RecentCall>()
    private val listeners = CopyOnWriteArraySet<Listener>()

    fun record(call: RecentCall) {
        synchronized(lock) {
            calls.removeAll { sameEndpoint(it, call.method, call.path) }
            calls.addFirst(call)
            while (calls.size > capacity) {
                calls.removeLast()
            }
        }
        notifyChanged()
    }

    // A mocked call never produces a real response, so the previously captured real body and status are kept
    // and only the timestamp and the flag move
    fun markMocked(
        method: String,
        path: String,
        statusCode: Int,
    ) {
        synchronized(lock) {
            val existing = calls.firstOrNull { sameEndpoint(it, method, path) }
            val updated =
                existing?.copy(timestamp = System.currentTimeMillis(), mocked = true)
                    ?: RecentCall(
                        method = method,
                        path = path,
                        statusCode = statusCode,
                        timestamp = System.currentTimeMillis(),
                        body = null,
                        contentType = null,
                        mocked = true,
                    )
            calls.removeAll { sameEndpoint(it, method, path) }
            calls.addFirst(updated)
            while (calls.size > capacity) {
                calls.removeLast()
            }
        }
        notifyChanged()
    }

    fun snapshot(): List<RecentCall> = synchronized(lock) { calls.toList() }

    fun findMatching(
        method: HttpMethod,
        pathPattern: String,
    ): RecentCall? =
        synchronized(lock) {
            calls.firstOrNull { method.matches(it.method) && RuleMatcher.pathMatches(pathPattern, it.path) }
        }

    fun clear() {
        synchronized(lock) { calls.clear() }
        notifyChanged()
    }

    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    private fun notifyChanged() {
        listeners.forEach { it.onRecentCallsChanged() }
    }

    private fun sameEndpoint(
        call: RecentCall,
        method: String,
        path: String,
    ): Boolean = call.method.equals(method, ignoreCase = true) && call.path == path

    companion object {
        private const val DEFAULT_CAPACITY = 50

        val shared: RecentCallStore by lazy { RecentCallStore() }
    }
}
