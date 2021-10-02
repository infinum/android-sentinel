package com.infinum.sentinel.ui.bundles.details

import android.os.Bundle
import android.text.format.Formatter
import android.view.View
import androidx.annotation.RestrictTo
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.infinum.sentinel.R
import com.infinum.sentinel.databinding.SentinelFragmentBundleDetailsBinding
import com.infinum.sentinel.ui.Presentation
import com.infinum.sentinel.ui.shared.base.BaseChildFragment
import com.infinum.sentinel.ui.shared.delegates.viewBinding
import com.infinum.sentinel.ui.shared.edgefactories.bounce.BounceEdgeEffectFactory
import org.koin.androidx.viewmodel.ext.android.viewModel

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class BundleDetailsFragment :
    BaseChildFragment<BundleDetailsState, Nothing>(R.layout.sentinel_fragment_bundle_details) {

    companion object {
        fun newInstance(bundleId: String?) = BundleDetailsFragment().apply {
            arguments = Bundle().apply {
                putString(Presentation.Constants.KEY_BUNDLE_ID, bundleId)
            }
        }

        const val TAG: String = "BundleDetailsFragment"
    }

    override val binding: SentinelFragmentBundleDetailsBinding by viewBinding(
        SentinelFragmentBundleDetailsBinding::bind
    )

    override val viewModel: BundleDetailsViewModel by viewModel()

    private var bundleId: String? = null

    private lateinit var adapter: BundleDetailsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bundleId = arguments?.getString(Presentation.Constants.KEY_BUNDLE_ID)
        viewModel.setBundleId(bundleId)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
    }

    override fun onState(state: BundleDetailsState) =
        when (state) {
            is BundleDetailsState.Data -> {
                binding.toolbar.subtitle = listOf(
                    state.value.className,
                    Formatter.formatFileSize(
                        binding.toolbar.context,
                        state.value.bundleTree.size.toLong()
                    )
                ).joinToString(" ~ ")

                adapter = BundleDetailsAdapter(state.value.bundleTree.size)
                binding.recyclerView.adapter = adapter
                adapter.submitList(state.value.bundleTree.subTrees)
            }
        }

    override fun onEvent(event: Nothing) = Unit

    private fun setupToolbar() {
        with(binding.toolbar) {
            setNavigationOnClickListener { requireActivity().finish() }
        }
    }

    private fun setupRecyclerView() =
        with(binding.recyclerView) {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            edgeEffectFactory = BounceEdgeEffectFactory()
            addItemDecoration(DividerItemDecoration(context, LinearLayoutManager.VERTICAL))
        }
}
