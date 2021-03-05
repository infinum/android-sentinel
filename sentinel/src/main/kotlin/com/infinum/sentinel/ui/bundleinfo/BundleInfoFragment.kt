package com.infinum.sentinel.ui.bundleinfo

import android.os.Bundle
import android.view.View
import androidx.annotation.RestrictTo
import com.infinum.sentinel.R
import com.infinum.sentinel.databinding.SentinelFragmentBundleInfoBinding
import com.infinum.sentinel.ui.shared.base.BaseChildFragment
import com.infinum.sentinel.ui.shared.delegates.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class BundleInfoFragment : BaseChildFragment(R.layout.sentinel_fragment_bundle_info) {

    companion object {
        fun newInstance() = BundleInfoFragment()
        val TAG: String = BundleInfoFragment::class.java.simpleName
    }

    override val binding: SentinelFragmentBundleInfoBinding by viewBinding(
        SentinelFragmentBundleInfoBinding::bind
    )

    override val viewModel: BundleInfoViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { requireActivity().finish() }
    }
}
