package com.infinum.sentinel.ui.children

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.infinum.sentinel.data.models.raw.AppInfo
import com.infinum.sentinel.data.sources.raw.DataSource
import com.infinum.sentinel.databinding.SentinelFragmentChildBinding
import com.infinum.sentinel.databinding.SentinelItemInfoBinding

class ApplicationFragment : Fragment() {

    companion object {
        fun newInstance() = ApplicationFragment()
        val TAG: String = ApplicationFragment::class.java.simpleName
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
            DataSource.applicationData
                .filterNot { entry -> entry.key == AppInfo.NAME }
                .forEach { applicationInfo ->
                    it.contentLayout.addView(
                        SentinelItemInfoBinding.inflate(
                            LayoutInflater.from(it.contentLayout.context),
                            it.contentLayout,
                            false
                        )
                            .apply {
                                labelView.text = applicationInfo.key.name.toUpperCase().capitalize().replace("_", " ")
                                valueView.text = applicationInfo.value
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
