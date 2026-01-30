package com.infinum.sentinel.data.sources.memory.certificate

import com.infinum.sentinel.data.models.raw.CertificateData

internal interface CertificateCache {
    fun save(value: CertificateData)

    fun load(): CertificateData

    fun clear()
}
