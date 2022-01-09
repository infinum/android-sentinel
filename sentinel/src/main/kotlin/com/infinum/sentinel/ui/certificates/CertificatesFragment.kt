package com.infinum.sentinel.ui.certificates

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.RestrictTo
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.infinum.sentinel.R
import com.infinum.sentinel.databinding.SentinelFragmentCertificatesBinding
import com.infinum.sentinel.ui.certificates.details.CertificateDetailsActivity
import com.infinum.sentinel.ui.shared.base.BaseChildFragment
import com.infinum.sentinel.ui.shared.delegates.viewBinding
import com.infinum.sentinel.ui.shared.edgefactories.bounce.BounceEdgeEffectFactory
import org.koin.androidx.viewmodel.ext.android.viewModel

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class CertificatesFragment :
    BaseChildFragment<CertificatesState, CertificatesEvent>(R.layout.sentinel_fragment_certificates) {

    companion object {
        fun newInstance() = CertificatesFragment()
        const val TAG: String = "CertificatesFragment"
    }

    override val binding: SentinelFragmentCertificatesBinding by viewBinding(
        SentinelFragmentCertificatesBinding::bind
    )

    override val viewModel: CertificatesViewModel by viewModel()

    private val adapter = CertificatesAdapter(
        onListChanged = { isEmpty ->
            showEmptyState(isEmpty)
        },
        onClick = {
            viewModel.cache(it)
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
    }

    override fun onState(state: CertificatesState) =
        when (state) {
            is CertificatesState.Data -> adapter.submitList(state.value)
        }

    override fun onEvent(event: CertificatesEvent) =
        when (event) {
            is CertificatesEvent.Cached -> {
                startActivity(
                    Intent(requireContext(), CertificateDetailsActivity::class.java)
                )
            }
        }

    private fun setupToolbar() {
        with(binding) {
            toolbar.setNavigationOnClickListener { requireActivity().finish() }
        }
    }

    private fun setupRecyclerView() {
        with(binding.recyclerView) {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = this@CertificatesFragment.adapter
            edgeEffectFactory = BounceEdgeEffectFactory()
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }
    }

    private fun showEmptyState(isEmpty: Boolean) =
        with(binding) {
            recyclerView.isGone = isEmpty
            emptyStateLayout.isVisible = isEmpty
        }
}
