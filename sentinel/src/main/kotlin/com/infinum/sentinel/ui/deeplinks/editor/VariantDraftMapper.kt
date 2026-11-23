package com.infinum.sentinel.ui.deeplinks.editor

import com.infinum.sentinel.domain.deeplinks.models.BaseLink
import com.infinum.sentinel.domain.deeplinks.models.BaseLinkOrigin
import com.infinum.sentinel.domain.deeplinks.models.DeepLinkUri
import com.infinum.sentinel.domain.deeplinks.models.HostSpec
import com.infinum.sentinel.domain.deeplinks.models.LinkSpec
import com.infinum.sentinel.domain.deeplinks.models.PathSpec
import com.infinum.sentinel.domain.deeplinks.models.QueryParam
import com.infinum.sentinel.domain.deeplinks.models.Variant
import com.infinum.sentinel.domain.deeplinks.uri.DeepLinkUriBuilder
import com.infinum.sentinel.domain.deeplinks.uri.DeepLinkUriEncoder

/**
 * Converts between base links, variants, drafts and links. Pure, no Android types.
 */
internal object VariantDraftMapper {
    /**
     * Builds the form for a new variant of [baseLink]. Null when the base link has no parsed shape.
     * An example is a finished link, so its host and path are fixed even when it has none, and its path is
     * kept exactly as written.
     */
    internal fun formFor(baseLink: BaseLink): VariantEditorForm? {
        val spec = baseLink.spec ?: return null
        val example = baseLink.exampleUri?.takeIf { baseLink.origin is BaseLinkOrigin.Example }
        return VariantEditorForm(
            scheme = spec.scheme,
            hasAuthority = example?.hasAuthority ?: true,
            port = spec.port,
            fragment = example?.fragment,
            spec = spec,
            hostMode = if (example != null) FieldMode.READ_ONLY else hostModeOf(spec.host),
            pathMode = if (example != null) FieldMode.READ_ONLY else pathModeOf(spec.path),
            fixedPath = example?.path,
            baseText = baseLink.displayText,
        )
    }

    /**
     * Builds the form for editing [variant]. Uses [baseLink] when it still exists and has a parsed shape,
     * otherwise everything is read-only and taken from the variant.
     */
    internal fun formFor(
        variant: Variant,
        baseLink: BaseLink?,
    ): VariantEditorForm {
        val fromBase = baseLink?.let { formFor(it) }
        // A fixed path keeps the saved text, so editing a variant never encodes its path again.
        return fromBase?.copy(fragment = variant.uri.fragment, fixedPath = fromBase.fixedPath?.let { variant.uri.path })
            ?: VariantEditorForm(
                scheme = variant.uri.scheme,
                hasAuthority = variant.uri.hasAuthority,
                port = variant.uri.port,
                fragment = variant.uri.fragment,
                spec = null,
                hostMode = FieldMode.READ_ONLY,
                pathMode = FieldMode.READ_ONLY,
                fixedPath = variant.uri.path,
                baseText = DeepLinkUriBuilder.build(variant.uri),
            )
    }

    /**
     * Creates the draft for a new variant. Literal parts and a path prefix are pre-filled, and an example
     * link brings its parameters in order.
     */
    internal fun newDraft(
        baseLink: BaseLink,
        form: VariantEditorForm,
    ): VariantDraft {
        val spec = form.spec
        val host = (spec?.host as? HostSpec.Literal)?.host.orEmpty()
        val path =
            when (val declared = spec?.path) {
                is PathSpec.Literal -> declared.value
                is PathSpec.Prefix -> declared.value
                else -> ""
            }
        val params = if (baseLink.origin is BaseLinkOrigin.Example) baseLink.exampleUri?.params.orEmpty() else listOf()
        val rows = rowsOf(params)
        return VariantDraft(
            variantId = null,
            baseKey = baseLink.key,
            componentName = (baseLink.origin as? BaseLinkOrigin.Declared)?.componentName,
            name = "",
            host = if (form.hostMode == FieldMode.READ_ONLY) host else "",
            path = path,
            rows = rows,
            nextRowId = rows.size.toLong(),
        )
    }

    /**
     * Creates the draft for editing [variant]. The path is decoded to plain text.
     */
    internal fun draftOf(variant: Variant): VariantDraft {
        val rows = rowsOf(variant.uri.params)
        return VariantDraft(
            variantId = variant.id,
            baseKey = variant.baseKey,
            componentName = variant.componentName,
            name = variant.name.orEmpty(),
            host = variant.uri.host.orEmpty(),
            path = DeepLinkUriEncoder.decodePath(variant.uri.path),
            rows = rows,
            nextRowId = rows.size.toLong(),
        )
    }

    /**
     * Builds the link parts from [draft]: host trimmed, keys trimmed, values untouched. The path is the form's
     * fixed path when it has one, otherwise the draft's path trimmed and encoded. Blank rows are left out.
     */
    internal fun toUri(
        draft: VariantDraft,
        form: VariantEditorForm,
    ): DeepLinkUri {
        val path =
            form.fixedPath ?: draft.path.trim().let { typed ->
                DeepLinkUriEncoder.encodePath(if (form.hasAuthority) LinkSpec.withLeadingSlash(typed) else typed)
            }
        return DeepLinkUri(
            scheme = form.scheme,
            hasAuthority = form.hasAuthority,
            host = draft.host.trim().ifEmpty { null },
            port = form.port,
            path = path,
            params = draft.rows.filterNot { it.isBlank }.map { QueryParam(it.key.trim(), it.value) },
            fragment = form.fragment,
        )
    }

    /**
     * The final link text for [draft].
     */
    internal fun toLink(
        draft: VariantDraft,
        form: VariantEditorForm,
    ): String = DeepLinkUriBuilder.build(toUri(draft, form))

    /**
     * Builds the variant to save. A new variant gets its id from [newId].
     */
    internal fun toVariant(
        draft: VariantDraft,
        form: VariantEditorForm,
        newId: () -> String,
    ): Variant =
        Variant(
            id = draft.variantId ?: newId(),
            name = draft.name.trim().ifEmpty { null },
            baseKey = draft.baseKey,
            componentName = draft.componentName,
            uri = toUri(draft, form),
        )

    private fun hostModeOf(host: HostSpec): FieldMode =
        when (host) {
            is HostSpec.Literal -> FieldMode.READ_ONLY
            HostSpec.AnyHost, is HostSpec.Wildcard -> FieldMode.REQUIRED
            HostSpec.None -> FieldMode.OPTIONAL
        }

    private fun pathModeOf(path: PathSpec): FieldMode =
        when (path) {
            is PathSpec.Literal -> FieldMode.READ_ONLY
            is PathSpec.Prefix, is PathSpec.Pattern, is PathSpec.AdvancedPattern, is PathSpec.Suffix -> FieldMode.REQUIRED
            PathSpec.None -> FieldMode.OPTIONAL
        }

    private fun rowsOf(params: List<QueryParam>): List<VariantDraftRow> =
        params.mapIndexed { index, param -> VariantDraftRow(index.toLong(), param.key, param.value) }
}
