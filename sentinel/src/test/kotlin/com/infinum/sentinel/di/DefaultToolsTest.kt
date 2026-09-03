package com.infinum.sentinel.di

import android.view.View
import com.infinum.sentinel.Sentinel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

internal class DefaultToolsTest {
    private class FakeTool(private val id: Int) : Sentinel.Tool {
        override fun name(): Int = id

        override fun listener(): View.OnClickListener = View.OnClickListener { }
    }

    private class OtherFakeTool : Sentinel.Tool {
        override fun name(): Int = 99

        override fun listener(): View.OnClickListener = View.OnClickListener { }
    }

    @Test
    fun `defaults are added when the user supplies none`() {
        val defaults = setOf<Sentinel.Tool>(FakeTool(1))

        val actual = resolveTools(userTools = emptySet(), defaults = defaults)

        assertEquals(defaults, actual)
    }

    @Test
    fun `a user tool of the same type replaces the default`() {
        val userTool = FakeTool(2)

        val actual = resolveTools(userTools = setOf(userTool), defaults = setOf(FakeTool(1)))

        assertEquals(1, actual.size)
        assertTrue(actual.first() === userTool)
    }

    @Test
    fun `a user tool of a different type is added alongside the defaults`() {
        val userTool = OtherFakeTool()

        val actual = resolveTools(userTools = setOf(userTool), defaults = setOf(FakeTool(1)))

        assertEquals(2, actual.size)
        assertTrue(actual.any { it === userTool })
    }
}
