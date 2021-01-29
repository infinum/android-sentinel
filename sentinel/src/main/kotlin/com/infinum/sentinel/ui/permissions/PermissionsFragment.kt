package com.infinum.sentinel.ui.permissions

import android.os.Bundle
import android.view.View
import androidx.annotation.RestrictTo
import com.infinum.sentinel.R
import com.infinum.sentinel.databinding.SentinelFragmentPermissionsBinding
import com.infinum.sentinel.databinding.SentinelViewItemCheckableBinding
import com.infinum.sentinel.ui.shared.base.BaseChildFragment
import com.infinum.sentinel.ui.shared.delegates.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class PermissionsFragment : BaseChildFragment(R.layout.sentinel_fragment_preferences) {

    companion object {
        fun newInstance() = PermissionsFragment()
        val TAG: String = PermissionsFragment::class.java.name
    }

    override val binding: SentinelFragmentPermissionsBinding by viewBinding(
        SentinelFragmentPermissionsBinding::bind
    )

    override val viewModel: PermissionsViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.data(this@PermissionsFragment::bind)
    }

    private fun bind(permissions: Map<String, Boolean>) =
        with(binding) {
            contentLayout.removeAllViews()
            permissions.forEach {
                contentLayout.addView(createItemView(it))
            }
        }

    private fun createItemView(entry: Map.Entry<String, Boolean>): View =
        SentinelViewItemCheckableBinding.inflate(layoutInflater, binding.contentLayout, false)
            .apply {
                this.labelView.text = entry.key
                this.valueView.setImageResource(
                    if (entry.value) {
                        R.drawable.sentinel_ic_checked
                    } else {
                        R.drawable.sentinel_ic_unchecked
                    }
                )
            }.root
}
