package com.infinum.sentinel.domain.deeplinks.discovery

import com.infinum.sentinel.domain.deeplinks.models.ConfigurationWarning
import com.infinum.sentinel.domain.deeplinks.models.DeclaredScreen

/**
 * Finds manifest problems that stop a declared link from working.
 */
internal object ConfigurationWarnings {
    private const val CATEGORY_DEFAULT = "android.intent.category.DEFAULT"
    private const val CATEGORY_BROWSABLE = "android.intent.category.BROWSABLE"

    /**
     * Reports missing categories. Without DEFAULT implicit intents (such as `startActivity` with a
     * link) do not match the filter; without BROWSABLE browsers and other apps cannot open it.
     */
    internal fun categoryWarnings(categories: List<String>): Set<ConfigurationWarning> =
        buildSet {
            if (CATEGORY_DEFAULT !in categories) add(ConfigurationWarning.MissingDefault)
            if (CATEGORY_BROWSABLE !in categories) add(ConfigurationWarning.MissingBrowsable)
        }

    /**
     * Adds [ConfigurationWarning.DuplicateDeclaration] to each link whose key is also declared by
     * other screens. Existing warnings, order of screens and order of links are kept. Disabled screens don't
     * count, because Android only chooses between enabled ones, and their own links get no duplicate warning.
     */
    internal fun attachDuplicates(screens: List<DeclaredScreen>): List<DeclaredScreen> {
        val keysPerScreen = screens.map { screen -> if (screen.isDisabled()) setOf() else screen.links.map { it.key }.toSet() }
        return screens.mapIndexed { index, screen ->
            val links =
                screen.links.map { link ->
                    val others =
                        screens
                            .filterIndexed { other, _ -> other != index && link.key in keysPerScreen[other] }
                            .map { it.title }
                    if (others.isEmpty() || screen.isDisabled()) {
                        link
                    } else {
                        link.copy(warnings = link.warnings + ConfigurationWarning.DuplicateDeclaration(others))
                    }
                }
            screen.copy(links = links)
        }
    }

    /** Every link of a disabled screen carries [ConfigurationWarning.Disabled], see BaseLinkExpander. */
    private fun DeclaredScreen.isDisabled(): Boolean = links.isNotEmpty() && links.all { ConfigurationWarning.Disabled in it.warnings }
}
