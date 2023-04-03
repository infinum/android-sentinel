package com.infinum.sentinel.data.sources.memory.certificate

import com.infinum.sentinel.data.models.raw.CertificateData
import me.tatarka.inject.annotations.Inject

@Inject
internal class InMemoryCertificateCache : CertificateCache {

    private var value: CertificateData? = null

    override fun save(value: CertificateData) {
        this.value = value
    }

    override fun load(): CertificateData {
        return this.value!!
    }

    override fun clear() {
        this.value = null
    }
}
