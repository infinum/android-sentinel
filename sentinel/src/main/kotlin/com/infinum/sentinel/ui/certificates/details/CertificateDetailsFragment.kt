package com.infinum.sentinel.ui.certificates.details

import android.os.Bundle
import android.view.View
import androidx.annotation.RestrictTo
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.infinum.sentinel.R
import com.infinum.sentinel.databinding.SentinelFragmentCertificateDetailsBinding
import com.infinum.sentinel.ui.shared.base.BaseChildFragment
import com.infinum.sentinel.ui.shared.delegates.viewBinding
import java.text.SimpleDateFormat
import org.koin.androidx.viewmodel.ext.android.viewModel

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class CertificateDetailsFragment :
    BaseChildFragment<CertificateDetailsState, Nothing>(R.layout.sentinel_fragment_certificate_details) {

    @Suppress("UNCHECKED_CAST")
    companion object {
        fun newInstance() = CertificateDetailsFragment()

        const val TAG: String = "CertificateDetailsFragment"
    }

    override val binding: SentinelFragmentCertificateDetailsBinding by viewBinding(
        SentinelFragmentCertificateDetailsBinding::bind
    )

    override val viewModel: CertificateDetailsViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(binding) {
            toolbar.setNavigationOnClickListener {
                requireActivity().finish()
            }
        }
    }

    @Suppress("NestedBlockDepth")
    override fun onState(state: CertificateDetailsState) {
        when (state) {
            is CertificateDetailsState.Cache -> {
                with(binding) {
                    toolbar.subtitle = state.value.title
                    publicKeyAlgorithmView.text = state.value.publicKey.algorithm
                    publicKeySizeView.text = state.value.publicKey.size.toString()
                    serialNumberView.text = state.value.serialNumber
                    versionView.text = state.value.version.toString()
                    signatureAlgorithmView.text = state.value.signature.algorithmName
                    signatureOidView.text = state.value.signature.algorithmOID
                    issuerView.text = state.value.issuerData.joinToString("\n")
                    subjectView.text = state.value.subjectData.joinToString("\n")
                    issuedView.text = SimpleDateFormat.getDateInstance().format(state.value.startDate)
                    expiresView.text = SimpleDateFormat.getDateInstance().format(state.value.endDate)
                    if (state.value.isValidNow) {
                        if (state.value.isValidIn(state.settings.expireInAmount, state.settings.expireInUnit)) {
                            expiredView.isVisible = false
                            expiredView.setBackgroundColor(
                                ContextCompat.getColor(
                                    expiredView.context,
                                    R.color.sentinel_color_primary
                                )
                            )
                            expiredView.text = getString(R.string.sentinel_expired)
                        } else {
                            expiredView.isVisible = true
                            expiredView.setBackgroundColor(
                                ContextCompat.getColor(
                                    expiredView.context,
                                    R.color.sentinel_warning
                                )
                            )
                            expiredView.text = String.format(getString(R.string.sentinel_expiring_at), expiresView.text)
                        }
                    } else {
                        expiredView.isVisible = true
                        expiredView.setBackgroundColor(
                            ContextCompat.getColor(
                                expiredView.context,
                                R.color.sentinel_error
                            )
                        )
                        expiredView.text = getString(R.string.sentinel_expired)
                    }
                    md5View.text = state.value.fingerprint.md5
                    sha1View.text = state.value.fingerprint.sha1
                    sha256View.text = state.value.fingerprint.sha256
                }
            }
        }
    }

    override fun onEvent(event: Nothing) = Unit
}
