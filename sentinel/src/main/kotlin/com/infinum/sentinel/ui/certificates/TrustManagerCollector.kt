package com.infinum.sentinel.ui.certificates

import java.io.IOException
import java.math.BigInteger
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
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

internal class TrustManagerCollector {

    private var userManagers: List<TrustManager> = listOf()

    fun setUserTrustManagers(managers: List<TrustManager>) {
        userManagers = managers.filterIsInstance<X509TrustManager>()
    }

    fun data(): List<CertificateData> =
        allTrustManagers()
            .map { it.acceptedIssuers.toList() }
            .toList()
            .flatten()
            .map {
                CertificateData(
                    publicKey = PublicKeyData(
                        algorithm = it.publicKey.algorithm,
                        size = when (it.publicKey.algorithm) {
                            "RSA" -> (it.publicKey as RSAPublicKey).modulus.bitLength()
                            "DSA" -> (it.publicKey as DSAPublicKey).params.p.bitLength() // Or P or Q or G?
                            "EC" -> (it.publicKey as ECPublicKey).params.order.bitLength() // Or curve or cofactor?
                            else -> it.publicKey.encoded.size * 7 // bad estimate and wild guess
                        }
                    ),
                    serialNumber = it.serialNumber.toString(16),
                    version = it.version,
                    signature = SignatureData(
                        algorithmName = it.sigAlgName,
                        algorithmOID = it.sigAlgOID
                    ),
                    issuerName = it.issuerDN.name,
                    subjectName = it.subjectDN.name,
                    startDate = it.notBefore,
                    endDate = it.notAfter,
                    fingerprint = FingerprintData(
                        md5 = fingerprint(it, "MD5")?.lowercase(),
                        sha1 = fingerprint(it, "SHA1")?.lowercase(),
                        sha256 = fingerprint(it, "SHA-256")?.lowercase()
                    )
                )
            }

    @Throws(
        NoSuchAlgorithmException::class,
        KeyStoreException::class,
        CertificateException::class,
        IOException::class
    )
    private fun defaultTrustManagers(): List<X509TrustManager> {
        val factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        val keyStore = KeyStore.getInstance("AndroidCAStore")
        keyStore.load(null, null)
        factory.init(keyStore)
        return factory.trustManagers.toList().filterIsInstance<X509TrustManager>()
    }

    private fun userTrustManagers(): List<X509TrustManager> =
        userManagers.filterIsInstance<X509TrustManager>()

    private fun allTrustManagers(): List<X509TrustManager> =
        userTrustManagers().plus(defaultTrustManagers())

    private fun fingerprint(certificate: X509Certificate, algorithm: String): String? {
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

internal fun ByteArray.asHexString(): String =
    String.format(
        "%0${this.size shl 1}X",
        BigInteger(1, this)
    )