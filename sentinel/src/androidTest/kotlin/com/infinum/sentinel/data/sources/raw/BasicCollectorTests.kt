package com.infinum.sentinel.data.sources.raw

import androidx.test.filters.SmallTest
import androidx.test.platform.app.InstrumentationRegistry
import com.infinum.sentinel.data.models.raw.BasicData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.BeforeClass
import org.junit.Test

internal class BasicCollectorTests {

    companion object {

        private const val EXPECTED_APPLICATION_NAME = "com.infinum.sentinel.test"

        private lateinit var actualBasicData: BasicData

        @BeforeClass
        @JvmStatic
        fun setupCollector() {
            val application = InstrumentationRegistry.getInstrumentation()
                .targetContext
                .applicationContext

            val collector = BasicCollector(application)
            collector.collect()

            actualBasicData = collector.present()
        }
    }

    @Test
    @SmallTest
    fun application_hasIcon() {
        assertNotNull(actualBasicData.applicationIcon)
    }

    @Test
    @SmallTest
    fun application_hasName() {
        assertNotNull(actualBasicData.applicationName)
        assertEquals(EXPECTED_APPLICATION_NAME, actualBasicData.applicationName)
    }

    @Test
    @SmallTest
    fun application_hasNoBlankName() {
        assertNotEquals("", actualBasicData.applicationName)
        assertEquals(EXPECTED_APPLICATION_NAME, actualBasicData.applicationName)
    }
}
