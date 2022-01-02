package com.infinum.sentinel.ui.crash

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.RestrictTo
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.infinum.sentinel.R
import com.infinum.sentinel.databinding.SentinelFragmentCrashesBinding
import com.infinum.sentinel.extensions.searchView
import com.infinum.sentinel.extensions.setup
import com.infinum.sentinel.ui.Presentation
import com.infinum.sentinel.ui.crash.details.CrashDetailsActivity
import com.infinum.sentinel.ui.shared.base.BaseChildFragment
import com.infinum.sentinel.ui.shared.delegates.viewBinding
import com.infinum.sentinel.ui.shared.edgefactories.bounce.BounceEdgeEffectFactory
import org.koin.androidx.viewmodel.ext.android.viewModel

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class CrashesFragment : BaseChildFragment<Nothing, CrashesEvent>(R.layout.sentinel_fragment_crashes) {

    companion object {
        fun newInstance(applicationName: String?) = CrashesFragment()
            .apply {
                arguments = Bundle().apply {
                    putString(Presentation.Constants.Keys.APPLICATION_NAME, applicationName)
                }
            }

        const val TAG: String = "CrashesFragment"
    }

    override val binding: SentinelFragmentCrashesBinding by viewBinding(
        SentinelFragmentCrashesBinding::bind
    )

    override val viewModel: CrashesViewModel by viewModel()

    private val adapter = CrashesAdapter(
        onListChanged = { isEmpty ->
            showEmptyState(isEmpty)
        },
        onClick = {
            startActivity(
                Intent(requireContext(), CrashDetailsActivity::class.java)
                    .apply {
                        putExtra(Presentation.Constants.Keys.CRASH_ID, it.id)
                    }
            )
        }
    )

    private var applicationName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        applicationName = arguments?.getString(Presentation.Constants.Keys.APPLICATION_NAME)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
    }

    override fun onState(state: Nothing) = Unit

    override fun onEvent(event: CrashesEvent) =
        when (event) {
            is CrashesEvent.CrashesIntercepted -> adapter.submitList(event.value)
        }

    private fun setupToolbar() {
        with(binding) {
            toolbar.setNavigationOnClickListener { requireActivity().finish() }
            toolbar.subtitle = applicationName
            toolbar.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.search -> {
                        toolbar.menu.findItem(R.id.clear).isVisible = false
                        true
                    }
                    R.id.clear -> {
                        viewModel.clearCrashes()
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
