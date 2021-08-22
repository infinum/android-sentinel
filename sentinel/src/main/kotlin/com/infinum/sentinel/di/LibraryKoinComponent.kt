package com.infinum.sentinel.di

import org.koin.core.Koin
import org.koin.core.component.KoinComponent

internal interface LibraryKoinComponent : KoinComponent {

    override fun getKoin(): Koin =
        LibraryKoin.koin()
}
