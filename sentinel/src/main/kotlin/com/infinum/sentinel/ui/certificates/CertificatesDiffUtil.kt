package com.infinum.sentinel.ui.certificates

import androidx.recyclerview.widget.DiffUtil
import com.infinum.sentinel.data.models.raw.CertificateData

internal class CertificatesDiffUtil : DiffUtil.ItemCallback<CertificateData>() {

    override fun areItemsTheSame(oldItem: CertificateData, newItem: CertificateData): Boolean {
        return oldItem.fingerprint.sha256 == newItem.fingerprint.sha256
    }

    override fun areContentsTheSame(oldItem: CertificateData, newItem: CertificateData): Boolean {
        return oldItem == newItem
    }
}
