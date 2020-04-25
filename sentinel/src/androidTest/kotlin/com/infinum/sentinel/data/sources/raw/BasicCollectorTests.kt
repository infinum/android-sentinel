package com.infinum.sentinel.data.sources.raw

import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.infinum.sentinel.data.models.raw.BasicData
import com.infinum.sentinel.ui.SentinelTestApplication
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class BasicCollectorTests {

    companion object {

        private const val EXPECTED_APPLICATION_LABEL = "Sentinel"

        lateinit var actualBasicData: BasicData

        @BeforeClass
        @JvmStatic
        fun setupCollector() {
            val application =
                ApplicationProvider.getApplicationContext<SentinelTestApplication>().applicationContext

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
    fun application_hasLabel() {
        assertNotNull(actualBasicData.applicationName)
        assertEquals(EXPECTED_APPLICATION_LABEL, actualBasicData.applicationName)
    }

    @Test
    @SmallTest
    fun application_hasNoBlankLabel() {
        assertNotEquals("", actualBasicData.applicationName)
        assertEquals(EXPECTED_APPLICATION_LABEL, actualBasicData.applicationName)
    }
}
