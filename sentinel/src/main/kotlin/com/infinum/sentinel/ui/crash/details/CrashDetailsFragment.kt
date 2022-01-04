package com.infinum.sentinel.ui.crash.details

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import androidx.annotation.RestrictTo
import androidx.core.app.ShareCompat
import androidx.core.view.isVisible
import com.infinum.sentinel.R
import com.infinum.sentinel.databinding.SentinelFragmentCrashDetailsBinding
import com.infinum.sentinel.databinding.SentinelViewItemThreadStateBinding
import com.infinum.sentinel.extensions.shareText
import com.infinum.sentinel.ui.Presentation
import com.infinum.sentinel.ui.shared.base.BaseChildFragment
import com.infinum.sentinel.ui.shared.delegates.viewBinding
import java.text.SimpleDateFormat
import java.util.Date
import org.koin.androidx.viewmodel.ext.android.viewModel

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class CrashDetailsFragment :
    BaseChildFragment<CrashDetailsState, CrashDetailsEvent>(R.layout.sentinel_fragment_crash_details) {

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

    @SuppressLint("SetTextI18n")
    @Suppress("NestedBlockDepth", "LongMethod")
    override fun onState(state: CrashDetailsState) =
        when (state) {
            is CrashDetailsState.Data -> {
                with(binding) {
                    toolbar.subtitle = state.value.applicationName
                    iconView.setImageResource(
                        if (state.value.data.exception?.isANRException == true) {
                            R.drawable.sentinel_ic_anr
                        } else {
                            R.drawable.sentinel_ic_crash
                        }
                    )
                    lineView.text = if (state.value.data.exception?.isANRException == true) {
                        lineView.context.getString(R.string.sentinel_anr_message)
                    } else {
                        listOfNotNull(
                            state.value.data.exception?.file,
                            state.value.data.exception?.lineNumber
                        ).joinToString(":")
                    }
                    timestampView.text = SimpleDateFormat.getDateTimeInstance().format(Date(state.value.timestamp))
                    exceptionView.text = if (state.value.data.exception?.isANRException == true) {
                        lineView.context.getString(R.string.sentinel_anr_title)
                    } else {
                        state.value.data.exception?.name
                    }
                    stackTraceView.text = "${state.value.data.exception?.name}: ${state.value.data.exception?.message}"
                        .plus(state.value.data.exception?.stackTrace?.joinToString { "\n\t\t\t at $it" })

                    if (state.value.data.exception?.isANRException == true) {
                        threadStatesLabel.text =
                            "${getString(R.string.sentinel_thread_states)}\t\t\t(${
                                state.value.data.threadState.orEmpty().count()
                            })"
                        state.value.data.threadState?.forEach { process ->
                            threadStatesContainer.addView(
                                SentinelViewItemThreadStateBinding.inflate(layoutInflater, threadStatesContainer, false)
                                    .apply {
                                        stackTraceView.text =
                                            "${process.name}\t\t\t${process.state.uppercase()}"
                                                .plus(process.stackTrace.joinToString { "\n\t\t\t at $it" })
                                    }.root
                            )
                        }
                        threadLabel.isVisible = false
                        threadView.isVisible = false
                        threadDataView.isVisible = false
                        threadStatesLabel.isVisible = true
                        threadStatesContainer.isVisible = true
                    } else {
                        threadView.text = listOf(
                            "${state.value.data.thread?.name}",
                            "${state.value.data.thread?.state?.uppercase()}"
                        ).joinToString("\t\t\t")
                        threadDataView.text = listOf(
                            "priority = ${
                                when (state.value.data.thread?.priority) {
                                    Thread.MAX_PRIORITY -> "maximum"
                                    Thread.MIN_PRIORITY -> "minimum"
                                    else -> "normal"
                                }
                            }",
                            "id = ${state.value.data.thread?.id}",
                            "daemon = ${state.value.data.thread?.isDaemon}"
                        ).joinToString("\t\t\t")
                        threadLabel.isVisible = true
                        threadView.isVisible = true
                        threadDataView.isVisible = true
                        threadStatesLabel.isVisible = false
                        threadStatesContainer.isVisible = false
                    }
                }
            }
        }

    override fun onEvent(event: CrashDetailsEvent) =
        when (event) {
            is CrashDetailsEvent.Removed -> requireActivity().finish()
            is CrashDetailsEvent.Formatted -> ShareCompat.IntentBuilder(requireActivity())
                .shareText(event.value)
        }

    private fun setupToolbar() {
        with(binding.toolbar) {
            setNavigationOnClickListener { requireActivity().finish() }
            setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.clear -> {
                        viewModel.remove()
                        true
                    }
                    R.id.share -> {
                        viewModel.share()
                        true
                    }
                    else -> false
                }
            }
        }
    }
}
