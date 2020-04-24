package com.infinum.sentinel.ui.children

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RestrictTo
import com.infinum.sentinel.data.sources.raw.DeviceCollector
import com.infinum.sentinel.databinding.SentinelFragmentDeviceBinding
import com.infinum.sentinel.ui.shared.BaseChildFragment
import org.koin.android.ext.android.get

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class DeviceFragment : BaseChildFragment<SentinelFragmentDeviceBinding>() {

    companion object {
        fun newInstance() = DeviceFragment()
        val TAG: String = DeviceFragment::class.java.simpleName
    }

    override fun provideViewBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ): SentinelFragmentDeviceBinding =
        SentinelFragmentDeviceBinding.inflate(inflater, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val collector: DeviceCollector = get()
        collector.collect()
        collector.present().let {
            with(viewBinding) {
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
