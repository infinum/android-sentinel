package com.infinum.sentinel.ui.shared

internal open class MemoizedSingleton<out T : Any, in A, in B>(creator: (A, B) -> T) {
    private var creator: ((A, B) -> T)? = creator

    @Volatile
    private var instance: T? = null

    internal fun memoized(context: A, tools: B): T {
        val instanceOld = instance
        instanceOld?.let {
            return instanceOld
        }

        return synchronized(this) {
            val instanceNew = instance
            instanceNew?.let {
                instanceNew
            } ?: run {
                val created = creator!!(context, tools)
                instance = created
                creator = null
                created
            }
        }
    }
}