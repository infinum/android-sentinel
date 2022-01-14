package com.infinum.sentinel.ui.certificates

import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.infinum.sentinel.R
import com.infinum.sentinel.data.models.local.CertificateMonitorEntity
import com.infinum.sentinel.data.models.raw.CertificateData
import com.infinum.sentinel.databinding.SentinelItemCertificateBinding
import java.time.temporal.ChronoUnit

internal class CertificateViewHolder(
    private val binding: SentinelItemCertificateBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(item: CertificateData?, settings: CertificateMonitorEntity? = null, onClick: (CertificateData) -> Unit) =
        item?.let { certificate ->
            with(binding) {
                titleView.text = certificate.title
                subtitleView.text = certificate.subtitle
                expiredView.setBackgroundColor(
                    ContextCompat.getColor(
                        expiredView.context,
                        if (certificate.isValidNow) {
                            if (
                                certificate.isValidIn(
                                    settings?.expireInAmount ?: 0,
                                    settings?.expireInUnit ?: ChronoUnit.DAYS
                                )
                            ) {
                                R.color.sentinel_color_primary
                            } else {
                                R.color.sentinel_warning
                            }
                        } else {
                            R.color.sentinel_error
                        }
                    )
                )
                root.setOnClickListener { onClick(certificate) }
            }
        } ?: unbind()

    fun unbind() =
        with(binding) {
            titleView.text = null
            subtitleView.text = null
            expiredView.background = null
            root.setOnClickListener(null)
        }
}
