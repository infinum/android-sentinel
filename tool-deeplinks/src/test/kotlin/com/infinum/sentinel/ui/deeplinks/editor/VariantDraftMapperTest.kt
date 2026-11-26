package com.infinum.sentinel.ui.deeplinks.editor

import com.infinum.sentinel.domain.deeplinks.discovery.ExampleLinkMapper
import com.infinum.sentinel.domain.deeplinks.models.BaseLink
import com.infinum.sentinel.domain.deeplinks.models.BaseLinkOrigin
import com.infinum.sentinel.domain.deeplinks.models.DeepLinkUri
import com.infinum.sentinel.domain.deeplinks.models.ExampleLinkData
import com.infinum.sentinel.domain.deeplinks.models.HostSpec
import com.infinum.sentinel.domain.deeplinks.models.LinkSpec
import com.infinum.sentinel.domain.deeplinks.models.PathSpec
import com.infinum.sentinel.domain.deeplinks.models.QueryParam
import com.infinum.sentinel.domain.deeplinks.models.Variant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Unit tests for [VariantDraftMapper].
 */
internal class VariantDraftMapperTest {
    private fun declared(spec: LinkSpec) = BaseLink("key", BaseLinkOrigin.Declared("com.example.Main"), spec, null)

    private fun example(raw: String) = ExampleLinkMapper.map(listOf(ExampleLinkData(raw, null))).single()

    private fun formOf(baseLink: BaseLink): VariantEditorForm = requireNotNull(VariantDraftMapper.formFor(baseLink))

    private fun draft(
        host: String = "",
        path: String = "",
        name: String = "",
        variantId: String? = null,
        rows: List<VariantDraftRow> = listOf(),
    ) = VariantDraft(variantId, "key", "com.example.Main", name, host, path, rows, rows.size.toLong())

    private fun literalForm(
        host: String,
        path: PathSpec,
    ) = formOf(declared(LinkSpec("myapp", HostSpec.Literal(host), null, path)))

    @Test
    fun `a path is encoded when the link is built`() {
        val form = literalForm("catalog", PathSpec.Prefix("/orders/"))
        val draft = draft(host = "catalog", path = "/orders/12 34")

        assertEquals("/orders/12%2034", VariantDraftMapper.toUri(draft, form).path)
        assertEquals("myapp://catalog/orders/12%2034", VariantDraftMapper.toLink(draft, form))
    }

    @Test
    fun `parameter values are encoded in order`() {
        val form = literalForm("product", PathSpec.Literal("/123"))
        val rows = listOf(VariantDraftRow(0, "promo", "SUMMER SALE"), VariantDraftRow(1, "ref", "push&email"))

        val link = VariantDraftMapper.toLink(draft(host = "product", path = "/123", rows = rows), form)

        assertEquals("myapp://product/123?promo=SUMMER%20SALE&ref=push%26email", link)
    }

    @Test
    fun `empty values and duplicate keys are kept`() {
        val form = literalForm("product", PathSpec.Literal("/123"))
        val rows =
            listOf(
                VariantDraftRow(0, "debug", ""),
                VariantDraftRow(1, "tag", "a"),
                VariantDraftRow(2, "tag", "b"),
            )

        val link = VariantDraftMapper.toLink(draft(host = "product", path = "/123", rows = rows), form)

        assertEquals("myapp://product/123?debug=&tag=a&tag=b", link)
    }

    @Test
    fun `host and key are trimmed and the value is kept as typed`() {
        val form = formOf(declared(LinkSpec("https", HostSpec.AnyHost, null, PathSpec.None)))
        val rows = listOf(VariantDraftRow(0, " promo ", " x "))

        val uri = VariantDraftMapper.toUri(draft(host = "  shop.example.com ", rows = rows), form)

        assertEquals("shop.example.com", uri.host)
        assertEquals(listOf(QueryParam("promo", " x ")), uri.params)
    }

    @Test
    fun `a blank name becomes null and a new variant gets the generated id`() {
        val form = literalForm("shop", PathSpec.None)

        val blank = VariantDraftMapper.toVariant(draft(host = "shop", name = "  "), form) { "new-id" }
        val named = VariantDraftMapper.toVariant(draft(host = "shop", name = "Promo"), form) { "new-id" }
        val existing = VariantDraftMapper.toVariant(draft(host = "shop", variantId = "v1"), form) { "new-id" }

        assertNull(blank.name)
        assertEquals("new-id", blank.id)
        assertEquals("Promo", named.name)
        assertEquals("v1", existing.id)
    }

    @Test
    fun `a new draft from an example brings its parameters in order`() {
        val base = example("myapp://product/123?ref=push&x=a+b")
        val form = formOf(base)

        val draft = VariantDraftMapper.newDraft(base, form)

        assertEquals(listOf(VariantDraftRow(0, "ref", "push"), VariantDraftRow(1, "x", "a b")), draft.rows)
        assertEquals(2L, draft.nextRowId)
        assertEquals("myapp://product/123?ref=push&x=a%20b", VariantDraftMapper.toLink(draft, form))
    }

    @Test
    fun `a wildcard host and a path prefix are required and the prefix is pre-filled`() {
        val base = declared(LinkSpec("https", HostSpec.Wildcard(".example.com"), null, PathSpec.Prefix("/orders/")))

        val form = formOf(base)
        val draft = VariantDraftMapper.newDraft(base, form)

        assertEquals(FieldMode.REQUIRED, form.hostMode)
        assertEquals(FieldMode.REQUIRED, form.pathMode)
        assertEquals("", draft.host)
        assertEquals("/orders/", draft.path)
    }

    @Test
    fun `a draft of a saved variant has plain text`() {
        val uri = DeepLinkUri("myapp", true, "shop", null, "/a%20b", listOf(QueryParam("q", "x")), null)
        val variant = Variant("v1", null, "key", null, uri)

        val draft = VariantDraftMapper.draftOf(variant)

        assertEquals("", draft.name)
        assertEquals("/a b", draft.path)
        assertEquals(listOf(VariantDraftRow(0, "q", "x")), draft.rows)
    }

    @Test
    fun `an editable path is trimmed`() {
        val form = literalForm("catalog", PathSpec.Prefix("/orders/"))

        assertEquals("myapp://catalog/orders/1", VariantDraftMapper.toLink(draft(host = "catalog", path = " /orders/1 "), form))
    }

    @Test
    fun `a declared literal path keeps its spaces`() {
        val base = declared(LinkSpec("myapp", HostSpec.Literal("shop"), null, PathSpec.Literal("/offer ")))
        val form = formOf(base)

        val link = VariantDraftMapper.toLink(VariantDraftMapper.newDraft(base, form), form)

        assertEquals("myapp://shop/offer%20", link)
    }

    @Test
    fun `a path without a leading slash gets one`() {
        val form = literalForm("open", PathSpec.None)

        assertEquals("myapp://open/orders", VariantDraftMapper.toLink(draft(host = "open", path = "orders"), form))
    }

    @Test
    fun `an example fragment is kept`() {
        val base = example("myapp://p#top")
        val form = formOf(base)

        assertEquals("myapp://p#top", VariantDraftMapper.toLink(VariantDraftMapper.newDraft(base, form), form))
    }

    @Test
    fun `an example path keeps its encoded characters`() {
        val base = example("myapp://open/url/https%3A%2F%2Fexample.com%2Fp")
        val form = formOf(base)

        assertEquals(
            "myapp://open/url/https%3A%2F%2Fexample.com%2Fp",
            VariantDraftMapper.toLink(VariantDraftMapper.newDraft(base, form), form),
        )
    }

    @Test
    fun `a variant whose base link is gone keeps its encoded path`() {
        val uri = DeepLinkUri("myapp", true, "shop", null, "/a%2Fb", listOf(), null)
        val variant = Variant("v1", null, "key", null, uri)

        val form = VariantDraftMapper.formFor(variant, null)

        assertEquals("myapp://shop/a%2Fb", VariantDraftMapper.toLink(VariantDraftMapper.draftOf(variant), form))
    }

    @Test
    fun `an example without a host has a fixed host and path`() {
        val base = example("myapp:settings")
        val form = formOf(base)

        assertEquals(FieldMode.READ_ONLY, form.hostMode)
        assertEquals(FieldMode.READ_ONLY, form.pathMode)
        assertEquals("myapp:settings", VariantDraftMapper.toLink(VariantDraftMapper.newDraft(base, form), form))
    }

    @Test
    fun `an example without a path has a fixed path`() {
        val form = formOf(example("https://example.com"))

        assertEquals(FieldMode.READ_ONLY, form.hostMode)
        assertEquals(FieldMode.READ_ONLY, form.pathMode)
    }

    @Test
    fun `a blank row is left out of the link`() {
        val form = literalForm("shop", PathSpec.Literal("/cart"))
        val rows = listOf(VariantDraftRow(0, "q", "x"), VariantDraftRow(1, "", ""))

        assertEquals("myapp://shop/cart?q=x", VariantDraftMapper.toLink(draft(host = "shop", path = "/cart", rows = rows), form))
    }
}
