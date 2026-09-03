package com.infinum.sentinel.ui.annotations

import android.os.Bundle
import android.view.View
import androidx.annotation.RestrictTo
import androidx.recyclerview.widget.LinearLayoutManager
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
            toolbar.setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.clear) viewModel.clear()
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
                adapter.submitList(state.annotations)
                val empty = state.annotations.isEmpty()
                binding.emptyLayout.visibility = if (empty) View.VISIBLE else View.GONE
                binding.actionsLayout.visibility = if (empty) View.GONE else View.VISIBLE
                binding.recyclerView.visibility = if (empty) View.GONE else View.VISIBLE
            }
        }

    override fun onEvent(event: AnnotationsEvent) =
        when (event) {
            // Close so the user lands on the live app with capture armed.
            is AnnotationsEvent.CaptureArmed -> requireActivity().finish()
            is AnnotationsEvent.ExportFinished -> showMessage(event.message)
            is AnnotationsEvent.ExportFailed -> showMessage(event.message)
        }

    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
}
