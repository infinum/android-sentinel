package com.infinum.sentinel.ui.bundles

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.RestrictTo
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.infinum.sentinel.R
import com.infinum.sentinel.databinding.SentinelFragmentBundlesBinding
import com.infinum.sentinel.ui.Presentation
import com.infinum.sentinel.ui.bundles.details.BundleDetailsActivity
import com.infinum.sentinel.ui.shared.base.BaseChildFragment
import com.infinum.sentinel.ui.shared.delegates.viewBinding
import com.infinum.sentinel.ui.shared.edgefactories.bounce.BounceEdgeEffectFactory
import org.koin.androidx.viewmodel.ext.android.viewModel

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class BundlesFragment : BaseChildFragment(R.layout.sentinel_fragment_bundles) {

    companion object {
        fun newInstance() = BundlesFragment()
        val TAG: String = BundlesFragment::class.java.simpleName
    }

    override val binding: SentinelFragmentBundlesBinding by viewBinding(
        SentinelFragmentBundlesBinding::bind
    )

    override val viewModel: BundlesViewModel by viewModel()

    private val adapter = BundlesAdapter {
        startActivity(
            Intent(requireContext(), BundleDetailsActivity::class.java)
                .apply {
                    putExtra(Presentation.Constants.KEY_BUNDLE_ID, it.bundleTree.id)
                }
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()

        viewModel.data {
            adapter.submitList(it)
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { requireActivity().finish() }
    }

    private fun setupRecyclerView() =
        with(binding) {
            recyclerView.layoutManager = LinearLayoutManager(recyclerView.context, LinearLayoutManager.VERTICAL, false)
            recyclerView.adapter = adapter
            recyclerView.edgeEffectFactory = BounceEdgeEffectFactory()
            recyclerView.addItemDecoration(DividerItemDecoration(recyclerView.context, LinearLayoutManager.VERTICAL))
        }
}
