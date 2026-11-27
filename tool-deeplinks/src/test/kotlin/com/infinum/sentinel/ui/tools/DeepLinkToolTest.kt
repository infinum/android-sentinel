package com.infinum.sentinel.ui.tools

import com.infinum.sentinel.domain.deeplinks.ExampleLinksRegistry
import com.infinum.sentinel.domain.deeplinks.models.ExampleLinkData
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for how [DeepLinkTool] hands its example links to the Deep links screens.
 */
internal class DeepLinkToolTest {
    @Test
    fun `creating the tool registers its example links`() {
        DeepLinkTool(listOf(DeepLinkTool.ExampleLink("myapp://home", "Home"), DeepLinkTool.ExampleLink("myapp://cart")))

        assertEquals(
            listOf(ExampleLinkData("myapp://home", "Home"), ExampleLinkData("myapp://cart", null)),
            ExampleLinksRegistry.links,
        )
    }

    @Test
    fun `a tool without example links clears the earlier ones`() {
        DeepLinkTool(listOf(DeepLinkTool.ExampleLink("myapp://home")))
        DeepLinkTool()

        assertEquals(listOf<ExampleLinkData>(), ExampleLinksRegistry.links)
    }
}
