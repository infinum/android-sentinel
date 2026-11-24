package com.infinum.sentinel.ui.deeplinks

import com.infinum.sentinel.ui.deeplinks.editor.VariantEditorTarget
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for [DeepLinksKey] serialization, which saves the Deep links back stack.
 */
internal class DeepLinksKeyTest {
    @Test
    fun `the list key survives saving`() {
        assertEquals(DeepLinksKey.Links, roundTrip(DeepLinksKey.Links))
    }

    @Test
    fun `a breakdown key survives saving`() {
        val key = DeepLinksKey.Breakdown(link = "myapp://shop/orders/42?promo=SUMMER")

        assertEquals(key, roundTrip(key))
    }

    @Test
    fun `an editor key for a new variant survives saving`() {
        val key =
            DeepLinksKey.Editor(
                target = VariantEditorTarget.NewFromBase(baseKey = "base-1", componentName = "com.example.MainActivity"),
            )

        assertEquals(key, roundTrip(key))
    }

    @Test
    fun `an editor key for a new variant without a component survives saving`() {
        val key =
            DeepLinksKey.Editor(
                target = VariantEditorTarget.NewFromBase(baseKey = "base-2", componentName = null),
            )

        assertEquals(key, roundTrip(key))
    }

    @Test
    fun `an editor key for a saved variant survives saving`() {
        val key = DeepLinksKey.Editor(target = VariantEditorTarget.Existing(variantId = "v1"))

        assertEquals(key, roundTrip(key))
    }

    @Test
    fun `a whole back stack survives saving in order`() {
        val backStack =
            listOf(
                DeepLinksKey.Links,
                DeepLinksKey.Editor(target = VariantEditorTarget.Existing(variantId = "v1")),
            )
        val serializer = ListSerializer(DeepLinksKey.serializer())

        assertEquals(backStack, Json.decodeFromString(serializer, Json.encodeToString(serializer, backStack)))
    }

    private fun roundTrip(key: DeepLinksKey): DeepLinksKey =
        Json.decodeFromString(DeepLinksKey.serializer(), Json.encodeToString(DeepLinksKey.serializer(), key))
}
