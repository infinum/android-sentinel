package com.infinum.sentinel.ui.annotations.overlay

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

internal class ComposeSourceLocationTest {
    @Test
    fun `formats file and line`() {
        assertEquals("CheckoutScreen.kt:142", ComposeSupport.formatSourceLocation("CheckoutScreen.kt", 142))
    }

    @Test
    fun `strips a leading path so the value stays greppable`() {
        assertEquals(
            "CheckoutScreen.kt:142",
            ComposeSupport.formatSourceLocation("com/example/checkout/CheckoutScreen.kt", 142),
        )
    }

    @Test
    fun `returns null without a source file`() {
        assertNull(ComposeSupport.formatSourceLocation(null, 142))
    }

    @Test
    fun `returns null for a non positive line number`() {
        assertNull(ComposeSupport.formatSourceLocation("CheckoutScreen.kt", -1))
    }
}
