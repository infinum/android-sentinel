package com.infinum.sentinel.ui.shared

open class SingletonHolder<out T : Any, in A, in B>(creator: (A, B) -> T) {
    private var creator: ((A, B) -> T)? = creator

    @Volatile
    private var instance: T? = null

    fun watch(context: A, tools: B): T {
        val i = instance
        if (i != null) {
            return i
        }

        return synchronized(this) {
            val i2 = instance
            if (i2 != null) {
                i2
            } else {
                val created = creator!!(context, tools)
                instance = created
                creator = null
                created
            }
        }
    }
}