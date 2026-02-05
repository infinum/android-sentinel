package com.infinum.sentinel.data.sources.raw.collectors

import android.os.Build
import com.infinum.sentinel.data.models.raw.CertificateData
import com.infinum.sentinel.data.models.raw.certificates.CertificateType
import com.infinum.sentinel.data.models.raw.certificates.FingerprintData
import com.infinum.sentinel.data.models.raw.certificates.PublicKeyData
import com.infinum.sentinel.data.models.raw.certificates.SignatureData
import com.infinum.sentinel.domain.collectors.Collectors
import com.infinum.sentinel.extensions.asASN
import com.infinum.sentinel.extensions.asHexString
import java.io.IOException
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateEncodingException
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.security.interfaces.DSAPublicKey
import java.security.interfaces.ECPublicKey
import java.security.interfaces.RSAPublicKey
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject

@Inject
internal class CertificateCollector(
    @Assisted var userCertificates: List<X509Certificate>,
) : Collectors.Certificates {
    companion object {
        private const val DEFAULT_PUBLIC_KEY_SIZE_MULTIPLIER = 7
        private const val SERIAL_NUMBER_RADIX = 16
    }

    override fun invoke(): Map<CertificateType, List<CertificateData>> =
        mapOf(
            CertificateType.USER to asCertificateData(userCertificates),
            CertificateType.SYSTEM to asCertificateData(defaultTrustedCertificates()),
        )

    private fun asCertificateData(certificates: List<X509Certificate>): List<CertificateData> =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            emptyList()
        } else {
            certificates
                .map {
                    CertificateData(
                        publicKey =
                        PublicKeyData(
                            algorithm = it.publicKey.algorithm,
                            size =
                            when (it.publicKey.algorithm) {
                                "RSA" -> (it.publicKey as RSAPublicKey).modulus.bitLength()

                                "DSA" -> (it.publicKey as DSAPublicKey).params.p.bitLength()

                                // Or P or Q or G?
                                "EC" -> (it.publicKey as ECPublicKey).params.order.bitLength()

                                // Or curve or cofactor?
                                else -> it.publicKey.encoded.size * DEFAULT_PUBLIC_KEY_SIZE_MULTIPLIER // wild guess
                            },
                        ),
                        serialNumber = it.serialNumber.toString(SERIAL_NUMBER_RADIX),
                        version = it.version,
                        signature =
                        SignatureData(
                            algorithmName = it.sigAlgName,
                            algorithmOID = it.sigAlgOID,
                        ),
                        issuerData = it.issuerDN.name.asASN(),
                        subjectData = it.subjectDN.name.asASN(),
                        startDate = it.notBefore,
                        endDate = it.notAfter,
                        fingerprint =
                        FingerprintData(
                            md5 = fingerprint(it, "MD5")?.lowercase(),
                            sha1 = fingerprint(it, "SHA1")?.lowercase(),
                            sha256 = fingerprint(it, "SHA-256")?.lowercase(),
                        ),
                    )
                }.toList()
                .sortedBy { it.title?.lowercase() }
        }

    @Throws(
        NoSuchAlgorithmException::class,
        KeyStoreException::class,
        CertificateException::class,
        IOException::class,
    )
    private fun defaultTrustedCertificates(): List<X509Certificate> =
        listOf(
            KeyStore.getInstance("AndroidCAStore"),
            KeyStore.getInstance(KeyStore.getDefaultType()),
        ).onEach { it.load(null, null) }
            .map {
                it
                    .aliases()
                    .toList()
                    .filter { alias -> it.entryInstanceOf(alias, KeyStore.TrustedCertificateEntry::class.java) }
                    .map { alias -> it.getCertificate(alias) as X509Certificate }
            }.flatten()

    @Suppress("SwallowedException")
    fun fingerprint(
        certificate: X509Certificate,
        algorithm: String,
    ): String? {
        var hash: String?
        try {
            val md = MessageDigest.getInstance(algorithm)
            hash = md.digest(certificate.encoded).asHexString()
            md.reset()
        } catch (e: CertificateEncodingException) {
            hash = null
        } catch (e: NoSuchAlgorithmException) {
            hash = null
        }
        return hash
    }
}
