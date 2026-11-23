package com.infinum.sentinel.ui.deeplinks.editor

import com.infinum.sentinel.domain.deeplinks.models.HostSpec
import com.infinum.sentinel.domain.deeplinks.models.LinkSpec
import com.infinum.sentinel.domain.deeplinks.models.PathSpec
import com.infinum.sentinel.domain.deeplinks.templates.LinkSpecMatcher
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for [VariantDraftValidator].
 */
internal class VariantDraftValidatorTest {
    private class FakeMatcher(
        private val answer: Boolean?,
    ) : LinkSpecMatcher {
        var callCount = 0

        override fun matches(
            spec: LinkSpec,
            link: String,
        ): Boolean? {
            callCount++
            return answer
        }
    }

    private val literalSpec = LinkSpec("myapp", HostSpec.Literal("shop"), null, PathSpec.Literal("/cart"))
    private val templateSpec = LinkSpec("https", HostSpec.Wildcard("example.com"), null, PathSpec.None)

    private fun form(
        spec: LinkSpec? = literalSpec,
        hostMode: FieldMode = FieldMode.READ_ONLY,
        pathMode: FieldMode = FieldMode.READ_ONLY,
    ) = VariantEditorForm(
        scheme = spec?.scheme ?: "myapp",
        hasAuthority = true,
        port = null,
        fragment = null,
        spec = spec,
        hostMode = hostMode,
        pathMode = pathMode,
        fixedPath = null,
        baseText = "",
    )

    private fun draft(
        host: String = "shop",
        path: String = "/cart",
        rows: List<VariantDraftRow> = listOf(),
    ) = VariantDraft(
        variantId = null,
        baseKey = "key",
        componentName = null,
        name = "",
        host = host,
        path = path,
        rows = rows,
        nextRowId = rows.size.toLong(),
    )

    private fun validate(
        draft: VariantDraft,
        form: VariantEditorForm = form(),
        matcher: LinkSpecMatcher = FakeMatcher(true),
    ) = VariantDraftValidator.validate(draft, form, matcher)

    @Test
    fun `a row with an empty key is an error and blocks saving`() {
        val result = validate(draft(rows = listOf(VariantDraftRow(7, "", "x"))))

        assertEquals(setOf(7L), result.emptyKeyRowIds)
        assertFalse(result.canSave)
    }

    @Test
    fun `a row with a blank key is an error and blocks saving`() {
        val result = validate(draft(rows = listOf(VariantDraftRow(3, "   ", "x"))))

        assertEquals(setOf(3L), result.emptyKeyRowIds)
        assertFalse(result.canSave)
    }

    @Test
    fun `a blank row is not an error`() {
        val result = validate(draft(rows = listOf(VariantDraftRow(4, "", ""), VariantDraftRow(5, "  ", " "))))

        assertEquals(setOf<Long>(), result.emptyKeyRowIds)
        assertTrue(result.canSave)
    }

    @Test
    fun `an empty value is fine`() {
        val result = validate(draft(rows = listOf(VariantDraftRow(0, "debug", ""))))

        assertEquals(setOf<Long>(), result.emptyKeyRowIds)
        assertTrue(result.canSave)
    }

    @Test
    fun `a required host that is empty blocks saving`() {
        val required = form(hostMode = FieldMode.REQUIRED)

        val empty = validate(draft(host = ""), required)
        val filled = validate(draft(host = "shop.example.com"), required)

        assertTrue(empty.isHostMissing)
        assertFalse(empty.canSave)
        assertFalse(filled.isHostMissing)
    }

    @Test
    fun `a required path that is empty blocks saving and a pre-filled prefix counts as filled`() {
        val required = form(pathMode = FieldMode.REQUIRED)

        val empty = validate(draft(path = ""), required)
        val filled = validate(draft(path = "/orders/"), required)

        assertTrue(empty.isPathMissing)
        assertFalse(empty.canSave)
        assertFalse(filled.isPathMissing)
    }

    @Test
    fun `optional host and path may be empty`() {
        val optional = form(hostMode = FieldMode.OPTIONAL, pathMode = FieldMode.OPTIONAL)

        val result = validate(draft(host = "", path = ""), optional)

        assertFalse(result.isHostMissing)
        assertFalse(result.isPathMissing)
        assertTrue(result.canSave)
    }

    @Test
    fun `a template that does not match gets a warning but can still be saved`() {
        val template = form(spec = templateSpec, hostMode = FieldMode.REQUIRED, pathMode = FieldMode.OPTIONAL)

        val result = validate(draft(host = "shop.example.com", path = ""), template, FakeMatcher(false))

        assertTrue(result.isPatternMismatch)
        assertTrue(result.canSave)
    }

    @Test
    fun `a matcher that cannot tell gives no warning`() {
        val template = form(spec = templateSpec, hostMode = FieldMode.REQUIRED, pathMode = FieldMode.OPTIONAL)

        val result = validate(draft(host = "shop.example.com", path = ""), template, FakeMatcher(null))

        assertFalse(result.isPatternMismatch)
    }

    @Test
    fun `a link that is not a template never asks the matcher`() {
        val matcher = FakeMatcher(false)

        val result = validate(draft(), form(), matcher)

        assertEquals(0, matcher.callCount)
        assertFalse(result.isPatternMismatch)
    }
}
