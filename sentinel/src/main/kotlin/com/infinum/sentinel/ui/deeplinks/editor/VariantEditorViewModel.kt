package com.infinum.sentinel.ui.deeplinks.editor

import com.infinum.sentinel.domain.Repositories
import com.infinum.sentinel.domain.deeplinks.models.BaseLink
import com.infinum.sentinel.domain.deeplinks.models.DeepLinksDiscovery
import com.infinum.sentinel.domain.deeplinks.models.LaunchMode
import com.infinum.sentinel.domain.deeplinks.models.Variant
import com.infinum.sentinel.domain.deeplinks.templates.LinkSpecMatcher
import com.infinum.sentinel.domain.deeplinks.variants.VariantsWriteException
import com.infinum.sentinel.ui.deeplinks.launch.LaunchRequest
import com.infinum.sentinel.ui.shared.base.BaseChildViewModel
import java.util.UUID
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Inject

@Inject
internal class VariantEditorViewModel(
    private val deepLinks: Repositories.DeepLinks,
    private val variants: Repositories.DeepLinkVariants,
) : BaseChildViewModel<VariantEditorState, VariantEditorEvent>() {
    private companion object {
        val format = Json { ignoreUnknownKeys = true }
    }

    /** The form and the draft it starts from, resolved off the main thread. */
    private data class Loaded(
        val form: VariantEditorForm,
        val draft: VariantDraft,
    )

    private val matcher: LinkSpecMatcher = AndroidLinkSpecMatcher()
    private var target: VariantEditorTarget? = null
    private var form: VariantEditorForm? = null
    private var initial: VariantDraft? = null
    private var current: VariantDraft? = null
    private var isShowingErrors = false
    private var isStarted = false
    private var isSaving = false

    /** Sets what the editor opens for. Call before [data]. */
    fun setTarget(value: VariantEditorTarget?) {
        target = value
    }

    /**
     * Restores a draft saved by [savedStateJson]. Ignored when a draft exists already or [json] can't be read.
     */
    fun restore(json: String) {
        if (current == null) {
            try {
                val saved = format.decodeFromString<VariantEditorSavedState>(json)
                initial = saved.initial
                current = saved.current
            } catch (ignored: SerializationException) {
                // A draft that can't be read is dropped, the editor starts from the saved variant.
            } catch (ignored: IllegalArgumentException) {
                // Same as above.
            }
        }
    }

    /** The draft as JSON for [restore], or null before it is loaded. */
    fun savedStateJson(): String? {
        val start = initial
        val draft = current
        return if (start == null || draft == null) null else format.encodeToString(VariantEditorSavedState(start, draft))
    }

    override fun data() {
        if (isStarted) return
        isStarted = true
        launch {
            val loaded = target?.let { io { resolve(it) } }
            if (loaded == null) {
                setState(VariantEditorState.Missing)
            } else {
                onLoaded(loaded)
            }
        }
    }

    private fun onLoaded(loaded: Loaded) {
        form = loaded.form
        // A draft restored after process death wins over the loaded one.
        if (current == null) {
            initial = loaded.draft
            current = loaded.draft
        }
        publish()
    }

    /** Sets the variant name as typed. */
    fun onNameChanged(value: String) = edit { it.copy(name = value) }

    /** Sets the host as typed. */
    fun onHostChanged(value: String) = edit { it.copy(host = value) }

    /** Sets the path as typed. */
    fun onPathChanged(value: String) = edit { it.copy(path = value) }

    /** Sets the key of the row with [rowId]. */
    fun onKeyChanged(
        rowId: Long,
        value: String,
    ) = edit { draft -> draft.copy(rows = draft.rows.map { if (it.rowId == rowId) it.copy(key = value) else it }) }

    /** Sets the value of the row with [rowId]. */
    fun onValueChanged(
        rowId: Long,
        value: String,
    ) = edit { draft -> draft.copy(rows = draft.rows.map { if (it.rowId == rowId) it.copy(value = value) else it }) }

    /** Adds an empty row at the end and asks the screen to focus it. */
    fun addRow() {
        appendRow()?.let { rowId ->
            publish()
            launch { emitEvent(VariantEditorEvent.FocusRow(rowId)) }
        }
    }

    /** Removes the row with [rowId]. */
    fun removeRow(rowId: Long) = edit { draft -> draft.copy(rows = draft.rows.filterNot { it.rowId == rowId }) }

    /**
     * Saves the variant and, when [launchMode] is set, asks the list to launch its link in that mode once the editor
     * closed. When the draft has errors, nothing is saved, the errors are shown and
     * [VariantEditorEvent.ValidationFailed] is emitted.
     */
    fun save(launchMode: LaunchMode? = null) {
        val currentForm = form
        val draft = current
        if (currentForm != null && draft != null && !isSaving) {
            if (VariantDraftValidator.validate(draft, currentForm, matcher).canSave) {
                persist(draft, currentForm, launchMode)
            } else {
                isShowingErrors = true
                publish()
                launch { emitEvent(VariantEditorEvent.ValidationFailed) }
            }
        }
    }

    private fun persist(
        draft: VariantDraft,
        currentForm: VariantEditorForm,
        launchMode: LaunchMode?,
    ) {
        isSaving = true
        launch {
            var isSaved = false
            try {
                val variant = VariantDraftMapper.toVariant(draft, currentForm) { UUID.randomUUID().toString() }
                io { variants.upsert(variant) }
                isSaved = true
                setState(VariantEditorState.Saved(launchMode?.let { LaunchRequest(VariantDraftMapper.toLink(draft, currentForm), it) }))
            } catch (ignored: VariantsWriteException) {
                emitEvent(VariantEditorEvent.SaveFailed)
            } finally {
                // After a successful save the screen is closing: keep Save blocked so a second tap can't store a copy.
                if (!isSaved) isSaving = false
            }
        }
    }

    private fun edit(transform: (VariantDraft) -> VariantDraft) {
        current = current?.let(transform)
        publish()
    }

    private fun appendRow(): Long? {
        val draft = current
        return draft?.let {
            current = it.copy(rows = it.rows + VariantDraftRow(it.nextRowId, "", ""), nextRowId = it.nextRowId + 1)
            it.nextRowId
        }
    }

    private fun publish() {
        val currentForm = form
        val draft = current
        val start = initial
        if (currentForm != null && draft != null && start != null) {
            setState(
                VariantEditorState.Data(
                    form = currentForm,
                    draft = draft,
                    validation = VariantDraftValidator.validate(draft, currentForm, matcher),
                    previewLink = VariantDraftMapper.toLink(draft, currentForm),
                    isDirty = draft != start,
                    isShowingErrors = isShowingErrors,
                ),
            )
        }
    }

    private suspend fun resolve(editorTarget: VariantEditorTarget): Loaded? {
        val discovery = deepLinks.discover()
        return when (editorTarget) {
            is VariantEditorTarget.NewFromBase -> loadBase(discovery, editorTarget)
            is VariantEditorTarget.Existing ->
                variants.find(editorTarget.variantId)?.let { variant -> loadVariant(discovery, variant) }
        }
    }

    private fun loadBase(
        discovery: DeepLinksDiscovery,
        editorTarget: VariantEditorTarget.NewFromBase,
    ): Loaded? {
        val base = findBaseLink(discovery, editorTarget.baseKey, editorTarget.componentName)
        val baseForm = base?.let { VariantDraftMapper.formFor(it) }
        return if (base == null || baseForm == null) null else Loaded(baseForm, VariantDraftMapper.newDraft(base, baseForm))
    }

    private fun loadVariant(
        discovery: DeepLinksDiscovery,
        variant: Variant,
    ): Loaded =
        Loaded(
            form = VariantDraftMapper.formFor(variant, findBaseLink(discovery, variant.baseKey, variant.componentName)),
            draft = VariantDraftMapper.draftOf(variant),
        )

    private fun findBaseLink(
        discovery: DeepLinksDiscovery,
        baseKey: String,
        componentName: String?,
    ): BaseLink? =
        discovery.examples.firstOrNull { it.key == baseKey }
            ?: discovery.screens.firstOrNull { screen ->
                screen.componentName == componentName && screen.links.any { it.key == baseKey }
            }?.links?.firstOrNull { it.key == baseKey }
            ?: discovery.screens.firstNotNullOfOrNull { screen -> screen.links.firstOrNull { it.key == baseKey } }
}
