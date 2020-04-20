package com.infinum.sentinel.ui.children

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.infinum.sentinel.data.sources.raw.DataSource
import com.infinum.sentinel.databinding.SentinelFragmentChildBinding
import com.infinum.sentinel.databinding.SentinelItemInfoBinding

class DeviceFragment : Fragment() {

    companion object {
        fun newInstance() = DeviceFragment()
        val TAG: String = DeviceFragment::class.java.simpleName
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
            DataSource.deviceData.forEach { deviceInfo ->
                it.contentLayout.addView(
                    SentinelItemInfoBinding.inflate(
                        LayoutInflater.from(it.contentLayout.context),
                        it.contentLayout,
                        false
                    )
                        .apply {
                            labelView.text = deviceInfo.key.name.toUpperCase().capitalize().replace("_", " ")
                            valueView.text = deviceInfo.value
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
