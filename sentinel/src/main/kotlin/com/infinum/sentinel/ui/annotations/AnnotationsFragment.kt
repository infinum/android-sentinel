package com.infinum.sentinel.ui.annotations

import android.os.Bundle
import android.view.View
import androidx.annotation.RestrictTo
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.infinum.sentinel.R
import com.infinum.sentinel.databinding.SentinelFragmentAnnotationsBinding
import com.infinum.sentinel.extensions.viewModels
import com.infinum.sentinel.ui.shared.base.BaseChildFragment
import com.infinum.sentinel.ui.shared.delegates.viewBinding

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class AnnotationsFragment : BaseChildFragment<AnnotationsState, AnnotationsEvent>(
    R.layout.sentinel_fragment_annotations,
) {
    companion object {
        fun newInstance() = AnnotationsFragment()

        const val TAG: String = "AnnotationsFragment"
    }

    override val viewModel: AnnotationsViewModel by viewModels()

    override val binding: SentinelFragmentAnnotationsBinding by viewBinding(
        SentinelFragmentAnnotationsBinding::bind,
    )

    private val adapter = AnnotationsAdapter(onDelete = { viewModel.delete(it.id) })

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            recyclerView.layoutManager = LinearLayoutManager(recyclerView.context, LinearLayoutManager.VERTICAL, false)
            recyclerView.adapter = adapter
            toolbar.setNavigationOnClickListener { requireActivity().finish() }
            toolbar.setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.clear) confirmClearAll()
                true
            }
            startButton.setOnClickListener { viewModel.arm() }
            enableButton.setOnClickListener { viewModel.arm() }
            exportButton.setIconResource(viewModel.exporterIcon())
            exportButton.setText(viewModel.exporterLabel())
            exportButton.setOnClickListener { viewModel.export(requireContext().applicationContext) }
        }
    }

    override fun onState(state: AnnotationsState) =
        when (state) {
            is AnnotationsState.Data -> {
                adapter.submitList(state.annotations.mapIndexed { index, annotation -> AnnotationRow(index + 1, annotation) })
                binding.exportButton.isEnabled = !state.isExporting
                val empty = state.annotations.isEmpty()
                binding.emptyLayout.visibility = if (empty) View.VISIBLE else View.GONE
                binding.actionsLayout.visibility = if (empty) View.GONE else View.VISIBLE
                binding.recyclerView.visibility = if (empty) View.GONE else View.VISIBLE
            }
        }

    override fun onEvent(event: AnnotationsEvent) =
        when (event) {
            // This Activity and SentinelActivity share a task affinity. finishAffinity finishes both,
            // so the host app comes forward. finishAndRemoveTask only removes the task when called
            // from its root activity, so here it would act like a plain finish.
            is AnnotationsEvent.CaptureArmed -> requireActivity().finishAffinity()
            is AnnotationsEvent.ExportFinished -> showMessage(event.message)
            is AnnotationsEvent.ExportFailed -> showMessage(event.message)
        }

    /**
     * Confirms before dropping every note, as deleting a single one does (see `AnnotationsAdapter`).
     * Notes live in memory only and there is no undo.
     */
    private fun confirmClearAll() {
        val count = adapter.itemCount
        // Nothing to confirm when the list is empty.
        if (count == 0) return
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.sentinel_annotations_clear_title)
            .setMessage(getString(R.string.sentinel_annotations_clear_message, count))
            .setNegativeButton(R.string.sentinel_cancel, null)
            .setPositiveButton(R.string.sentinel_clear) { _, _ -> viewModel.clear() }
            .show()
    }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
}
