package com.infinum.sentinel.ui.deeplinks.editor

import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import androidx.core.widget.doAfterTextChanged
import com.infinum.sentinel.R
import com.infinum.sentinel.databinding.SentinelItemVariantParamBinding

/**
 * Keeps the parameter rows of the editor in [container]. When the list of row ids changes, only the rows
 * that disappeared are removed and only the new ones are inflated, so existing rows keep their views and
 * focus and typing never resets a field or moves its cursor. Edits are forwarded to the callbacks.
 */
internal class VariantParamRowsBinder(
    private val container: LinearLayout,
    private val onKeyChanged: (Long, String) -> Unit,
    private val onValueChanged: (Long, String) -> Unit,
    private val onRemoveClick: (Long) -> Unit,
) {
    private val rows = mutableMapOf<Long, SentinelItemVariantParamBinding>()
    private var renderedIds: List<Long>? = null

    /**
     * Shows [draftRows]. Returns true when rows were added or removed, false when the ids were the same.
     */
    fun bind(draftRows: List<VariantDraftRow>): Boolean {
        val ids = draftRows.map { it.rowId }
        val isChanged = ids != renderedIds
        if (isChanged) {
            val keptIds = ids.toSet()
            rows.keys
                .filter { it !in keptIds }
                .forEach { rowId -> rows.remove(rowId)?.let { container.removeView(it.root) } }
            // Rows are only added or removed, never reordered, so the draft index is the container index.
            draftRows.forEachIndexed { index, draftRow -> if (draftRow.rowId !in rows) addRow(draftRow, index) }
            renderedIds = ids
        }
        return isChanged
    }

    /**
     * Shows the key error on the rows in [emptyKeyRowIds] when [isShowingErrors] is true, clears it otherwise.
     */
    fun showErrors(
        emptyKeyRowIds: Set<Long>,
        isShowingErrors: Boolean,
    ) {
        rows.forEach { (rowId, row) ->
            row.keyInputLayout.error =
                if (isShowingErrors && rowId in emptyKeyRowIds) {
                    container.context.getString(R.string.sentinel_deep_links_editor_key_required)
                } else {
                    null
                }
        }
    }

    /** The key field of the row with [rowId], or null when that row isn't shown. */
    fun keyInputOf(rowId: Long): View? = rows[rowId]?.keyInput

    /** Removes all rows. */
    fun clear() {
        container.removeAllViews()
        rows.clear()
        renderedIds = null
    }

    private fun addRow(
        draftRow: VariantDraftRow,
        index: Int,
    ) {
        val row = SentinelItemVariantParamBinding.inflate(LayoutInflater.from(container.context), container, false)
        // The rows share view ids, so their text comes from the draft and not from saved view state.
        // Inflated rows must not share ids either, or the window could restore focus into the wrong row.
        listOf(row.keyInputLayout, row.keyInput, row.valueInputLayout, row.valueInput).forEach {
            it.isSaveEnabled = false
            it.id = View.NO_ID
        }
        row.keyInput.setText(draftRow.key)
        row.valueInput.setText(draftRow.value)
        row.keyInput.doAfterTextChanged { onKeyChanged(draftRow.rowId, it?.toString().orEmpty()) }
        row.valueInput.doAfterTextChanged { onValueChanged(draftRow.rowId, it?.toString().orEmpty()) }
        row.removeButton.setOnClickListener { onRemoveClick(draftRow.rowId) }
        rows[draftRow.rowId] = row
        container.addView(row.root, index)
    }
}
