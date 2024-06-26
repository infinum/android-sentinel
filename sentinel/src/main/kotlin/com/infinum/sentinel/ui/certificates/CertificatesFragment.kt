package com.infinum.sentinel.ui.certificates

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.RequiresApi
import androidx.annotation.RestrictTo
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.infinum.sentinel.R
import com.infinum.sentinel.databinding.SentinelFragmentCertificatesBinding
import com.infinum.sentinel.extensions.viewModels
import com.infinum.sentinel.ui.certificates.details.CertificateDetailsActivity
import com.infinum.sentinel.ui.shared.base.BaseChildFragment
import com.infinum.sentinel.ui.shared.delegates.viewBinding
import com.infinum.sentinel.ui.shared.edgefactories.bounce.BounceEdgeEffectFactory

@RestrictTo(RestrictTo.Scope.LIBRARY)
@RequiresApi(Build.VERSION_CODES.O)
internal class CertificatesFragment :
    BaseChildFragment<CertificatesState, CertificatesEvent>(R.layout.sentinel_fragment_certificates) {

    companion object {
        fun newInstance() = CertificatesFragment()
        const val TAG: String = "CertificatesFragment"
    }

    override val binding: SentinelFragmentCertificatesBinding by viewBinding(
        SentinelFragmentCertificatesBinding::bind
    )

    override val viewModel: CertificatesViewModel by viewModels()

    private val userHeaderAdapter = HeaderAdapter(R.string.sentinel_certificates_user, 0)
    private val userAdapter = CertificatesAdapter(
        onListChanged = {
            showEmptyState()
        },
        onClick = {
            viewModel.cache(it)
        }
    )
    private val systemHeaderAdapter = HeaderAdapter(R.string.sentinel_certificates_system, 0)
    private val systemAdapter = CertificatesAdapter(
        onListChanged = {
            showEmptyState()
        },
        onClick = {
            viewModel.cache(it)
        }
    )
    private val adapter = ConcatAdapter(
        userHeaderAdapter,
        userAdapter,
        systemHeaderAdapter,
        systemAdapter
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
    }

    override fun onState(state: CertificatesState) {
        when (state) {
            is CertificatesState.Data -> {
                userAdapter.settings = state.settings
                userHeaderAdapter.updateCount(state.userCertificates.count())
                systemHeaderAdapter.updateCount(state.systemCertificates.count())
                userAdapter.submitList(state.userCertificates)
                systemAdapter.submitList(state.systemCertificates)
                if (state.userCertificates.isEmpty()) {
                    adapter.removeAdapter(userHeaderAdapter)
                }
                if (state.systemCertificates.isEmpty()) {
                    adapter.removeAdapter(systemHeaderAdapter)
                }
            }
        }
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

    private fun showEmptyState() {
        val isUserEmpty: Boolean = userAdapter.currentList.isEmpty()
        val isSystemEmpty: Boolean = systemAdapter.currentList.isEmpty()
        userHeaderAdapter.updateCount(userAdapter.currentList.count())
        systemHeaderAdapter.updateCount(systemAdapter.currentList.count())
        with(binding) {
            recyclerView.isGone = isUserEmpty && isSystemEmpty
            emptyStateLayout.isVisible = isUserEmpty && isSystemEmpty
        }
    }
}
