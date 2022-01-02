package com.infinum.sentinel.ui.crash.details

import android.os.Bundle
import android.view.View
import androidx.annotation.RestrictTo
import com.infinum.sentinel.R
import com.infinum.sentinel.databinding.SentinelFragmentCrashDetailsBinding
import com.infinum.sentinel.ui.Presentation
import com.infinum.sentinel.ui.shared.base.BaseChildFragment
import com.infinum.sentinel.ui.shared.delegates.viewBinding
import java.text.SimpleDateFormat
import java.util.Date
import org.koin.androidx.viewmodel.ext.android.viewModel

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class CrashDetailsFragment :
    BaseChildFragment<CrashDetailsState, Nothing>(R.layout.sentinel_fragment_crash_details) {

    companion object {
        fun newInstance(crashId: Long) = CrashDetailsFragment().apply {
            arguments = Bundle().apply {
                putLong(Presentation.Constants.Keys.CRASH_ID, crashId)
            }
        }

        const val TAG: String = "CrashDetailsFragment"
    }

    override val binding: SentinelFragmentCrashDetailsBinding by viewBinding(
        SentinelFragmentCrashDetailsBinding::bind
    )

    override val viewModel: CrashDetailsViewModel by viewModel()

    private var crashId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        crashId = arguments?.getLong(Presentation.Constants.Keys.CRASH_ID)
        crashId?.let { viewModel.setCrashId(it) } ?: activity?.finish()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
    }

    override fun onState(state: CrashDetailsState) =
        when (state) {
            is CrashDetailsState.Data -> {
                with(binding) {
                    toolbar.subtitle = state.value.applicationName
                    lineView.text = listOfNotNull(
                        state.value.data.exception?.file,
                        state.value.data.exception?.lineNumber
                    ).joinToString(":")
                    timestampView.text = SimpleDateFormat.getTimeInstance().format(Date(state.value.timestamp))
                    exceptionView.text = state.value.data.exception?.name
                }
            }
        }

    override fun onEvent(event: Nothing) = Unit

    private fun setupToolbar() {
        with(binding.toolbar) {
            setNavigationOnClickListener { requireActivity().finish() }
        }
    }
}
