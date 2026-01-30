package com.infinum.sentinel.data.sources.raw.collectors

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.ui.tools.DummyTool
import com.infinum.sentinel.ui.tools.NoNameTool
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
internal class ToolsCollectorTests {
    companion object {
        private lateinit var dummyTool: DummyTool
        private lateinit var noNameTool: NoNameTool

        @BeforeClass
        @JvmStatic
        fun setupCollector() {
            dummyTool = DummyTool()
            noNameTool = NoNameTool()
        }
    }

    @Test
    @SmallTest
    fun tools_AreEmpty() {
        val collector = ToolsCollector(setOf())
        val expectedTools = setOf<Sentinel.Tool>()

        val actualTools = collector()

        assertEquals(expectedTools.size, actualTools.size)
        assertTrue(actualTools.isEmpty())
        assertEquals(expectedTools, actualTools)
    }

    @Test
    @SmallTest
    fun tools_AreUnique() {
        val collector = ToolsCollector(setOf(dummyTool, dummyTool))
        val expectedTools = setOf(dummyTool)

        val actualTools = collector()

        assertEquals(expectedTools.size, actualTools.size)
        assertTrue(actualTools.isNotEmpty())
        assertEquals(expectedTools, actualTools)
    }

    @Test
    @SmallTest
    fun tools_AreValid() {
        val collector = ToolsCollector(setOf(dummyTool, noNameTool))
        val expectedTools = setOf(dummyTool)

        val actualTools = collector()

        assertEquals(expectedTools.size, actualTools.size)
        assertTrue(actualTools.isNotEmpty())
        assertEquals(expectedTools, actualTools)
    }
}
