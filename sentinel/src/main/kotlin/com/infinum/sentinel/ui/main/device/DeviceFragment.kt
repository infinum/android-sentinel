package com.infinum.sentinel.ui.main.device

import androidx.annotation.RestrictTo
import com.infinum.sentinel.R
import com.infinum.sentinel.databinding.SentinelFragmentDeviceBinding
import com.infinum.sentinel.ui.shared.base.BaseChildFragment
import com.infinum.sentinel.ui.shared.delegates.viewBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class DeviceFragment : BaseChildFragment<DeviceState, Nothing>(R.layout.sentinel_fragment_device) {

    companion object {
        fun newInstance() = DeviceFragment()
        const val TAG: String = "DeviceFragment"
    }

    override val binding: SentinelFragmentDeviceBinding by viewBinding(
        SentinelFragmentDeviceBinding::bind
    )

    override val viewModel: DeviceViewModel by viewModel()

    override fun onState(state: DeviceState) =
        when (state) {
            is DeviceState.Data -> with(binding) {
                manufacturerView.data = state.value.manufacturer
                modelView.data = state.value.model
                idView.data = state.value.id
                bootloaderView.data = state.value.bootloader
                deviceView.data = state.value.device
                boardView.data = state.value.board
                architecturesView.data = state.value.architectures
                codenameView.data = state.value.codename
                releaseView.data = state.value.release
                sdkView.data = state.value.sdk
                securityPatchView.data = state.value.securityPatch
                emulatorView.data = state.value.isProbablyAnEmulator.toString()
                autoTimeView.data = state.value.autoTime.toString()
                autoTimezoneView.data = state.value.autoTimezone.toString()
            }
        }

    override fun onEvent(event: Nothing) = Unit
}
