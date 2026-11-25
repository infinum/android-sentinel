package com.infinum.sentinel.ui.deeplinks.list

import com.infinum.sentinel.domain.deeplinks.models.BaseLink
import com.infinum.sentinel.domain.deeplinks.models.BaseLinkOrigin
import com.infinum.sentinel.domain.deeplinks.models.DeepLinkUri
import com.infinum.sentinel.domain.deeplinks.models.Variant
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [LinkRowAction.forRow].
 */
internal class LinkRowActionTest {
    @Test
    fun `a declared template offers only Breakdown`() {
        assertEquals(listOf(LinkRowAction.BREAKDOWN), LinkRowAction.forRow(baseRow(launchLink = null)))
    }

    @Test
    fun `a base link offers Breakdown, Copy and Share`() {
        assertEquals(
            listOf(LinkRowAction.BREAKDOWN, LinkRowAction.COPY, LinkRowAction.SHARE),
            LinkRowAction.forRow(baseRow(launchLink = "myapp://home")),
        )
    }

    @Test
    fun `a variant offers every entry in menu order`() {
        assertEquals(
            listOf(
                LinkRowAction.EDIT,
                LinkRowAction.DUPLICATE,
                LinkRowAction.BREAKDOWN,
                LinkRowAction.COPY,
                LinkRowAction.SHARE,
                LinkRowAction.DELETE,
            ),
            LinkRowAction.forRow(variantRow()),
        )
    }

    private fun baseLink() = BaseLink(key = "k", origin = BaseLinkOrigin.Declared("com.example.A"), spec = null, exampleUri = null)

    private fun baseRow(launchLink: String?) =
        DeepLinksItem.BaseLinkRow(
            baseLink = baseLink(),
            variantCount = 0,
            title = null,
            link = launchLink ?: "myapp://product/*",
            launchLink = launchLink,
            status = null,
        )

    private fun variant() =
        Variant(
            id = "v1",
            name = null,
            baseKey = "k",
            componentName = null,
            uri = DeepLinkUri("myapp", true, "home", null, "", listOf(), null),
        )

    private fun variantRow() =
        DeepLinksItem.VariantRow(
            variant = variant(),
            baseLink = baseLink(),
            isOrphan = false,
            title = null,
            link = "myapp://home",
            status = null,
        )
}
