package com.infinum.sentinel.ui.children

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RestrictTo
import androidx.fragment.app.Fragment
import com.infinum.sentinel.data.sources.raw.DataSource
import com.infinum.sentinel.databinding.SentinelFragmentChildBinding
import com.infinum.sentinel.databinding.SentinelItemToolBinding

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class ToolsFragment : Fragment() {

    companion object {
        fun newInstance() = ToolsFragment()
        val TAG: String = ToolsFragment::class.java.simpleName
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
            DataSource.toolsData.forEach { external ->
                it.contentLayout.addView(
                    SentinelItemToolBinding.inflate(
                        LayoutInflater.from(it.contentLayout.context),
                        it.contentLayout,
                        false
                    )
                        .apply {
                            externalButton.text = getString(external.name())
                            externalButton.setOnClickListener(external.listener())
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
