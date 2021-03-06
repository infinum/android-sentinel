package com.infinum.sentinel.ui.bundlemonitor

import android.os.Bundle
import android.view.View
import androidx.annotation.RestrictTo
import com.infinum.sentinel.R
import com.infinum.sentinel.databinding.SentinelFragmentBundleMonitorBinding
import com.infinum.sentinel.ui.shared.base.BaseChildFragment
import com.infinum.sentinel.ui.shared.delegates.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import timber.log.Timber

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class BundleMonitorFragment : BaseChildFragment(R.layout.sentinel_fragment_bundle_monitor) {

    companion object {
        fun newInstance() = BundleMonitorFragment()
        val TAG: String = BundleMonitorFragment::class.java.simpleName
    }

    override val binding: SentinelFragmentBundleMonitorBinding by viewBinding(
        SentinelFragmentBundleMonitorBinding::bind
    )

    override val viewModel: BundleMonitorViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()

        viewModel.bundles {
            Timber.tag("_BOJAN_3").i(it.toString())
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { requireActivity().finish() }
    }
}
