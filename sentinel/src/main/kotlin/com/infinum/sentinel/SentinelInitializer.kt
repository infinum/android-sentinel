package com.infinum.sentinel

import android.content.Context
import androidx.startup.Initializer
import com.infinum.sentinel.di.LibraryKoin
import com.infinum.sentinel.ui.Presentation

class SentinelInitializer : Initializer<Class<SentinelInitializer>> {

    override fun create(context: Context): Class<SentinelInitializer> {

        LibraryKoin.init(context)

        Presentation.init(context)
        
        return SentinelInitializer::class.java
    }

    override fun dependencies(): List<Class<out Initializer<*>>> =
        listOf()
}
