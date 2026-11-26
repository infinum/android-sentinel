package com.infinum.sentinel.ui.deeplinks.list

import com.infinum.sentinel.domain.deeplinks.models.DeepLinkUri
import com.infinum.sentinel.domain.deeplinks.models.QueryParam
import com.infinum.sentinel.domain.deeplinks.models.Variant
import com.infinum.sentinel.domain.deeplinks.overview.DeepLinksOverview
import com.infinum.sentinel.domain.deeplinks.overview.DeepLinksSection
import com.infinum.sentinel.domain.deeplinks.overview.VariantEntry
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [flatten].
 */
internal class DeepLinksItemTest {
    @Test
    fun `a variant row launches the encoded link and shows the readable one`() {
        val uri = DeepLinkUri("myapp", true, "shop", null, "/a%20b", listOf(QueryParam("q", "summer sale & more")), null)
        val overview =
            DeepLinksOverview(
                notices = listOf(),
                sections = listOf(DeepLinksSection.SavedVariants(listOf(VariantEntry(Variant("v1", null, "key", null, uri))))),
                isEmpty = false,
            )

        val row = overview.flatten().filterIsInstance<DeepLinksItem.VariantRow>().single()

        assertEquals("myapp://shop/a%20b?q=summer%20sale%20%26%20more", row.launchLink)
        assertEquals("myapp://shop/a b?q=summer sale %26 more", row.displayLink)
    }
}
