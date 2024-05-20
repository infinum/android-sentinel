package com.infinum.sentinel.ui.certificates

import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.ListAdapter
import com.infinum.sentinel.data.models.local.CertificateMonitorEntity
import com.infinum.sentinel.data.models.raw.CertificateData
import com.infinum.sentinel.databinding.SentinelItemCertificateBinding

@RequiresApi(Build.VERSION_CODES.O)
internal class CertificatesAdapter(
    private val onListChanged: () -> Unit,
    private val onClick: (CertificateData) -> Unit
) : ListAdapter<CertificateData, CertificateViewHolder>(CertificatesDiffUtil()) {

    var settings: CertificateMonitorEntity? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CertificateViewHolder =
        CertificateViewHolder(
            SentinelItemCertificateBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: CertificateViewHolder, position: Int) =
        holder.bind(getItem(position), settings, onClick)

    override fun onViewRecycled(holder: CertificateViewHolder) {
        holder.unbind()
    }

    override fun onCurrentListChanged(
        previousList: MutableList<CertificateData>,
        currentList: MutableList<CertificateData>
    ) = onListChanged()
}
