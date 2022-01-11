package com.infinum.sentinel.ui.certificates.observer

internal data class CertificateCount(
    val invalid: Int,
    val toExpire: Int
)
