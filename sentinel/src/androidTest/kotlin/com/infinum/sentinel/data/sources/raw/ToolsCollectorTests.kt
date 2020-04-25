package com.infinum.sentinel.data.sources.raw

import androidx.test.filters.SmallTest
import com.infinum.sentinel.ui.tools.DummyTool
import com.infinum.sentinel.ui.tools.NoNameTool
import com.infinum.sentinel.ui.tools.AppInfoTool
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Test

internal class ToolsCollectorTests {

    companion object {

        private lateinit var dummyTool: DummyTool
        private lateinit var noNameTool: NoNameTool

        @BeforeClass
        @JvmStatic
        fun setupCollector() {
            dummyTool =
                DummyTool()
            noNameTool =
                NoNameTool()
        }
    }

    @Test
    @SmallTest
    fun tools_AreEmpty() {
        val collector = ToolsCollector(setOf())
        collector.collect()
        val expectedTools = setOf(AppInfoTool())

        val actualTools = collector.present()

        assertEquals(expectedTools.size, actualTools.size)
        assertTrue(actualTools.isNotEmpty())
        assertEquals(expectedTools, actualTools)
    }

    @Test
    @SmallTest
    fun tools_AreUnique() {
        val collector = ToolsCollector(setOf(dummyTool, dummyTool))
        collector.collect()
        val expectedTools = setOf(dummyTool, AppInfoTool())

        val actualTools = collector.present()

        assertEquals(expectedTools.size, actualTools.size)
        assertTrue(actualTools.isNotEmpty())
        assertEquals(expectedTools, actualTools)
    }

    @Test
    @SmallTest
    fun tools_AreValid() {
        val collector = ToolsCollector(setOf(dummyTool, noNameTool))
        collector.collect()
        val expectedTools = setOf(dummyTool, AppInfoTool())

        val actualTools = collector.present()

        assertEquals(expectedTools.size, actualTools.size)
        assertTrue(actualTools.isNotEmpty())
        assertEquals(expectedTools, actualTools)
    }
}
