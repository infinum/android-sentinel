package com.infinum.sentinel.ui.certificates

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DiffUtil
import com.infinum.sentinel.data.models.raw.CertificateData

internal class CertificatesDiffUtil : DiffUtil.ItemCallback<CertificateData>() {
    override fun areItemsTheSame(
        oldItem: CertificateData,
        newItem: CertificateData,
    ): Boolean = oldItem.fingerprint.sha256 == newItem.fingerprint.sha256

    @RequiresApi(Build.VERSION_CODES.O)
    override fun areContentsTheSame(
        oldItem: CertificateData,
        newItem: CertificateData,
    ): Boolean = oldItem == newItem
}
