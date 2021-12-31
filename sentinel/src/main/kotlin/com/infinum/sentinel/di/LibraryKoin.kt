package com.infinum.sentinel.di

import android.content.Context
import com.infinum.sentinel.ui.Presentation
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.Koin
import org.koin.core.KoinApplication
import org.koin.core.logger.Level

internal object LibraryKoin {

    private val koinApplication = KoinApplication.init()

    fun koin(): Koin = koinApplication.koin

    fun initialize(context: Context) {
        koinApplication.apply {
            androidLogger(Level.ERROR)
            androidContext(context)
            modules(Presentation.modules())
        }
    }
}
