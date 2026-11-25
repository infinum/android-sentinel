package com.infinum.sentinel.domain.deeplinks.discovery

import com.infinum.sentinel.domain.deeplinks.models.BaseLink
import com.infinum.sentinel.domain.deeplinks.models.BaseLinkOrigin
import com.infinum.sentinel.domain.deeplinks.models.ConfigurationWarning
import com.infinum.sentinel.domain.deeplinks.models.DeclaredScreen
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [ConfigurationWarnings].
 */
internal class ConfigurationWarningsTest {
    private val default = "android.intent.category.DEFAULT"
    private val browsable = "android.intent.category.BROWSABLE"

    private fun link(
        key: String,
        warnings: Set<ConfigurationWarning> = setOf(),
    ): BaseLink = BaseLink(key, BaseLinkOrigin.Declared("com.example.Any"), null, null, warnings)

    private fun screen(
        title: String,
        vararg links: BaseLink,
    ): DeclaredScreen = DeclaredScreen("com.example.$title", title, links.toList())

    @Test
    fun `missing default is reported`() {
        assertEquals(setOf(ConfigurationWarning.MissingDefault), ConfigurationWarnings.categoryWarnings(listOf(browsable)))
    }

    @Test
    fun `missing browsable is reported`() {
        assertEquals(setOf(ConfigurationWarning.MissingBrowsable), ConfigurationWarnings.categoryWarnings(listOf(default)))
    }

    @Test
    fun `both categories present gives no warning`() {
        assertEquals(setOf<ConfigurationWarning>(), ConfigurationWarnings.categoryWarnings(listOf(default, browsable)))
    }

    @Test
    fun `no categories reports both`() {
        assertEquals(
            setOf(ConfigurationWarning.MissingDefault, ConfigurationWarning.MissingBrowsable),
            ConfigurationWarnings.categoryWarnings(listOf()),
        )
    }

    @Test
    fun `duplicate across two screens is reported on both and keeps existing warnings`() {
        val result =
            ConfigurationWarnings.attachDuplicates(
                listOf(
                    screen("Main", link("K", setOf(ConfigurationWarning.MissingBrowsable))),
                    screen("Promo", link("K")),
                ),
            )

        assertEquals(
            setOf(ConfigurationWarning.MissingBrowsable, ConfigurationWarning.DuplicateDeclaration(listOf("Promo"))),
            result[0].links[0].warnings,
        )
        assertEquals(setOf(ConfigurationWarning.DuplicateDeclaration(listOf("Main"))), result[1].links[0].warnings)
    }

    @Test
    fun `duplicate across three screens lists the other two in order`() {
        val result =
            ConfigurationWarnings.attachDuplicates(
                listOf(screen("Main", link("K")), screen("Promo", link("K")), screen("Sale", link("K"))),
            )

        assertEquals(
            setOf(ConfigurationWarning.DuplicateDeclaration(listOf("Promo", "Sale"))),
            result[0].links[0].warnings,
        )
    }

    @Test
    fun `unique link keeps its warnings`() {
        val unique = link("U", setOf(ConfigurationWarning.MissingDefault))
        val result =
            ConfigurationWarnings.attachDuplicates(listOf(screen("Main", unique, link("K")), screen("Promo", link("K"))))

        assertEquals(unique, result[0].links[0])
    }

    @Test
    fun `a disabled screen gets no duplicate warning, keeps its own and doesn't count for others`() {
        val disabled = link("K", setOf(ConfigurationWarning.Disabled, ConfigurationWarning.MissingBrowsable))
        val result = ConfigurationWarnings.attachDuplicates(listOf(screen("Main", link("K")), screen("Promo", disabled)))

        assertEquals(setOf<ConfigurationWarning>(), result[0].links[0].warnings)
        assertEquals(setOf(ConfigurationWarning.Disabled, ConfigurationWarning.MissingBrowsable), result[1].links[0].warnings)
    }

    @Test
    fun `enabled screens name only each other when a disabled screen declares the same link`() {
        val result =
            ConfigurationWarnings.attachDuplicates(
                listOf(
                    screen("Main", link("K")),
                    screen("Promo", link("K")),
                    screen("Sale", link("K", setOf(ConfigurationWarning.Disabled))),
                ),
            )

        assertEquals(setOf(ConfigurationWarning.DuplicateDeclaration(listOf("Promo"))), result[0].links[0].warnings)
        assertEquals(setOf(ConfigurationWarning.DuplicateDeclaration(listOf("Main"))), result[1].links[0].warnings)
        assertEquals(setOf(ConfigurationWarning.Disabled), result[2].links[0].warnings)
    }
}
