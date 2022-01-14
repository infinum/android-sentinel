package com.infinum.sentinel.domain.certificate

import com.infinum.sentinel.data.models.raw.CertificateData
import com.infinum.sentinel.data.sources.memory.certificate.CertificateCache
import com.infinum.sentinel.domain.Repositories
import com.infinum.sentinel.domain.certificate.models.CertificateParameters

internal class CertificateRepository(
    private val memoryCache: CertificateCache
) : Repositories.Certificate {

    override fun cache(cache: CertificateParameters.Cache) {
        memoryCache.save(cache.value)
    }

    override fun consume(): CertificateData {
        val item = memoryCache.load()
        memoryCache.clear()
        return item
    }
}
