package com.infinum.sentinel.ui.bundles

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.RestrictTo
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.infinum.sentinel.R
import com.infinum.sentinel.databinding.SentinelFragmentBundlesBinding
import com.infinum.sentinel.extensions.searchView
import com.infinum.sentinel.extensions.setup
import com.infinum.sentinel.extensions.viewModels
import com.infinum.sentinel.ui.bundles.details.BundleDetailsActivity
import com.infinum.sentinel.ui.shared.Constants
import com.infinum.sentinel.ui.shared.base.BaseChildFragment
import com.infinum.sentinel.ui.shared.delegates.viewBinding
import com.infinum.sentinel.ui.shared.edgefactories.bounce.BounceEdgeEffectFactory

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class BundlesFragment : BaseChildFragment<Nothing, BundlesEvent>(R.layout.sentinel_fragment_bundles) {

    companion object {
        fun newInstance() = BundlesFragment()
        const val TAG: String = "BundlesFragment"
    }

    override val binding: SentinelFragmentBundlesBinding by viewBinding(
        SentinelFragmentBundlesBinding::bind
    )

    override val viewModel: BundlesViewModel by viewModels()

    private val adapter = BundlesAdapter(
        onListChanged = { isEmpty ->
            showEmptyState(isEmpty)
        },
        onClick = {
            startActivity(
                Intent(requireContext(), BundleDetailsActivity::class.java)
                    .apply {
                        putExtra(Constants.Keys.BUNDLE_ID, it.bundleTree.id)
                    }
            )
        }
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
    }

    override fun onState(state: Nothing) = Unit

    override fun onEvent(event: BundlesEvent) =
        when (event) {
            is BundlesEvent.BundlesIntercepted -> adapter.submitList(event.value)
        }

    private fun setupToolbar() {
        with(binding) {
            toolbar.setNavigationOnClickListener { requireActivity().finish() }
            toolbar.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.search -> {
                        toolbar.menu.findItem(R.id.clear).isVisible = false
                        true
                    }
                    R.id.clear -> {
                        viewModel.clearBundles()
                        true
                    }
                    else -> false
                }
            }
            toolbar.menu.searchView?.setup(
                hint = getString(R.string.sentinel_search),
                onSearchClosed = {
                    toolbar.menu.findItem(R.id.clear).isVisible = true
                    viewModel.data()
                },
                onQueryTextChanged = { query ->
                    viewModel.setSearchQuery(query)
                }
            )
        }
    }

    private fun setupRecyclerView() {
        with(binding) {
            recyclerView.layoutManager = LinearLayoutManager(recyclerView.context, LinearLayoutManager.VERTICAL, false)
            recyclerView.adapter = adapter
            recyclerView.edgeEffectFactory = BounceEdgeEffectFactory()
            recyclerView.addItemDecoration(DividerItemDecoration(recyclerView.context, LinearLayoutManager.VERTICAL))
        }
    }

    private fun showEmptyState(isEmpty: Boolean) =
        with(binding) {
            recyclerView.isGone = isEmpty
            emptyStateLayout.isVisible = isEmpty
        }
}
