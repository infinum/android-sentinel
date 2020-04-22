package com.infinum.sentinel.ui.children

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RestrictTo
import androidx.fragment.app.Fragment
import com.infinum.sentinel.R
import com.infinum.sentinel.data.sources.raw.DataSource
import com.infinum.sentinel.databinding.SentinelFragmentChildBinding
import com.infinum.sentinel.databinding.SentinelItemPermissionBinding

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class PermissionsFragment : Fragment() {

    companion object {
        fun newInstance() = PermissionsFragment()
        val TAG: String = PermissionsFragment::class.java.simpleName
    }

    private var viewBinding: SentinelFragmentChildBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewBinding = SentinelFragmentChildBinding.inflate(inflater, container, false)
        return viewBinding?.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding?.let {
            DataSource.permissions.forEach { permission ->
                it.contentLayout.addView(
                    SentinelItemPermissionBinding.inflate(
                        LayoutInflater.from(it.contentLayout.context),
                        it.contentLayout,
                        false
                    )
                        .apply {
                            labelView.text = permission.key
                            valueView.setImageResource(
                                if (permission.value) {
                                    R.drawable.sentinel_ic_checked
                                } else {
                                    R.drawable.sentinel_ic_unchecked
                                }
                            )
                        }.root
                )
            }
        }
    }

    override fun onDestroy() =
        super.onDestroy().run {
            viewBinding = null
        }
}
