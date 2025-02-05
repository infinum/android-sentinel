package com.infinum.sentinel.ui.main.permissions

import android.view.View
import androidx.annotation.RestrictTo
import com.infinum.sentinel.R
import com.infinum.sentinel.databinding.SentinelFragmentPermissionsBinding
import com.infinum.sentinel.databinding.SentinelViewItemCheckableBinding
import com.infinum.sentinel.extensions.copyToClipboard
import com.infinum.sentinel.extensions.viewModels
import com.infinum.sentinel.ui.shared.base.BaseChildFragment
import com.infinum.sentinel.ui.shared.delegates.viewBinding

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class PermissionsFragment :
    BaseChildFragment<PermissionsState, Nothing>(R.layout.sentinel_fragment_all_preferences) {

    companion object {
        fun newInstance() = PermissionsFragment()
        const val TAG: String = "PermissionsFragment"
    }

    override val binding: SentinelFragmentPermissionsBinding by viewBinding(
        SentinelFragmentPermissionsBinding::bind
    )

    override val viewModel: PermissionsViewModel by viewModels()

    override fun onState(state: PermissionsState) =
        when (state) {
            is PermissionsState.Data -> with(binding) {
                contentLayout.removeAllViews()
                state.value.forEach {
                    contentLayout.addView(createItemView(it))
                }
            }
        }

    override fun onEvent(event: Nothing) = Unit

    private fun createItemView(entry: Map.Entry<String, Boolean>): View =
        SentinelViewItemCheckableBinding.inflate(layoutInflater, binding.contentLayout, false)
            .apply {
                this.labelView.text = entry.key
                this.valueView.isChecked = entry.value
                root.setOnLongClickListener {
                    it.context.copyToClipboard(
                        key = entry.key,
                        value = "${entry.key} = ${entry.value}"
                    )
                }
            }.root
}
