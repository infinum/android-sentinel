package com.infinum.sentinel

import android.content.Context
import androidx.startup.Initializer
import com.infinum.sentinel.ui.DependencyGraph

class SentinelInitializer : Initializer<Class<SentinelInitializer>> {

    override fun create(context: Context): Class<SentinelInitializer> {
        DependencyGraph.initialise(context)
        return SentinelInitializer::class.java
    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> =
        mutableListOf()
}