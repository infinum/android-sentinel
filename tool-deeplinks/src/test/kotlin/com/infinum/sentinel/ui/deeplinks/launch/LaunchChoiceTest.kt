package com.infinum.sentinel.ui.deeplinks.launch

import com.infinum.sentinel.domain.deeplinks.models.ConfigurationWarning
import com.infinum.sentinel.domain.deeplinks.models.LaunchMode
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [LaunchChoice.forLink].
 */
internal class LaunchChoiceTest {
    private val noBrowsable = setOf(ConfigurationWarning.MissingBrowsable)

    @Test
    fun `a web link asks for the mode`() {
        assertEquals(LaunchChoice.Ask, LaunchChoice.forLink("https://example.com/offers", setOf()))
    }

    @Test
    fun `a web link with an upper case scheme asks for the mode`() {
        assertEquals(LaunchChoice.Ask, LaunchChoice.forLink("HTTP://example.com", setOf()))
    }

    @Test
    fun `a custom scheme link opens with Real tap`() {
        assertEquals(LaunchChoice.Direct(LaunchMode.REAL_TAP), LaunchChoice.forLink("myapp://home", setOf()))
    }

    @Test
    fun `a custom scheme link without BROWSABLE opens in this app`() {
        assertEquals(LaunchChoice.Direct(LaunchMode.OPEN_IN_APP), LaunchChoice.forLink("myapp://home", noBrowsable))
    }

    @Test
    fun `a web link without BROWSABLE opens in this app without asking`() {
        assertEquals(LaunchChoice.Direct(LaunchMode.OPEN_IN_APP), LaunchChoice.forLink("https://example.com", noBrowsable))
    }

    @Test
    fun `a link without BROWSABLE and DEFAULT keeps the usual choice`() {
        val warnings = setOf(ConfigurationWarning.MissingBrowsable, ConfigurationWarning.MissingDefault)

        assertEquals(LaunchChoice.Direct(LaunchMode.REAL_TAP), LaunchChoice.forLink("myapp://home", warnings))
        assertEquals(LaunchChoice.Ask, LaunchChoice.forLink("https://example.com", warnings))
    }

    @Test
    fun `a link without BROWSABLE on a disabled screen keeps the usual choice`() {
        val warnings = setOf(ConfigurationWarning.MissingBrowsable, ConfigurationWarning.Disabled)

        assertEquals(LaunchChoice.Direct(LaunchMode.REAL_TAP), LaunchChoice.forLink("myapp://home", warnings))
    }

    @Test
    fun `a web link without BROWSABLE on a disabled screen asks for the mode`() {
        val warnings = setOf(ConfigurationWarning.MissingBrowsable, ConfigurationWarning.Disabled)

        assertEquals(LaunchChoice.Ask, LaunchChoice.forLink("https://example.com", warnings))
    }

    @Test
    fun `a link on a disabled screen keeps the usual choice`() {
        assertEquals(LaunchChoice.Direct(LaunchMode.REAL_TAP), LaunchChoice.forLink("myapp://home", setOf(ConfigurationWarning.Disabled)))
    }

    @Test
    fun `a duplicate declaration doesn't change the in app choice`() {
        val warnings = setOf(ConfigurationWarning.MissingBrowsable, ConfigurationWarning.DuplicateDeclaration(listOf("Promo")))

        assertEquals(LaunchChoice.Direct(LaunchMode.OPEN_IN_APP), LaunchChoice.forLink("myapp://home", warnings))
    }
}
