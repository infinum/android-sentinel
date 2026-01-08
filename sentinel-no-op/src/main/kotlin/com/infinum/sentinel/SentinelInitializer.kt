package com.infinum.sentinel

import android.content.Context
import androidx.startup.Initializer

public class SentinelInitializer : Initializer<Class<SentinelInitializer>> {
    override fun create(context: Context): Class<SentinelInitializer> {
        // no-op implementation
        return SentinelInitializer::class.java
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = listOf()
}
