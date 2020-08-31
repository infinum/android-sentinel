package com.infinum.sentinel.ui.children

import android.os.Bundle
import android.view.View
import androidx.annotation.RestrictTo
import com.infinum.sentinel.R
import com.infinum.sentinel.databinding.SentinelFragmentDeviceBinding
import com.infinum.sentinel.ui.DependencyGraph
import com.infinum.sentinel.ui.shared.BaseChildFragment
import com.infinum.sentinel.ui.shared.viewBinding

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class DeviceFragment : BaseChildFragment(R.layout.sentinel_fragment_device) {

    companion object {
        fun newInstance() = DeviceFragment()
        val TAG: String = DeviceFragment::class.java.simpleName
    }

    override val binding: SentinelFragmentDeviceBinding by viewBinding(
        SentinelFragmentDeviceBinding::bind
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        DependencyGraph.collectors().device().let {
            with(binding) {
                manufacturerView.data = it.manufacturer
                modelView.data = it.model
                idView.data = it.id
                bootloaderView.data = it.bootloader
                deviceView.data = it.device
                boardView.data = it.board
                architecturesView.data = it.architectures
                codenameView.data = it.codename
                releaseView.data = it.release
                sdkView.data = it.sdk
                securityPatchView.data = it.securityPatch
                emulatorView.data = it.isProbablyAnEmulator.toString()
            }
        }
    }
}
