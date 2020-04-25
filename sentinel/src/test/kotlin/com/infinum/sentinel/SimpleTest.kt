package com.infinum.sentinel

import com.infinum.sentinel.data.sources.raw.BasicCollector
import com.infinum.sentinel.di.KoinDI
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.test.KoinTest
import org.koin.test.get

internal class SimpleTest : KoinTest {

    @Test
    fun myTest() {
        // You can start your Koin configuration
        startKoin { modules(KoinDI.modules) }

        // or directly get any instance
        val basicCollector : BasicCollector = get()
        basicCollector.collect()
        val d = basicCollector.present().applicationName

        assertEquals("", d)
    }
}