package com.infinum.sentinel.ui.certificates

internal sealed class CertificatesEvent {

    class Cached : CertificatesEvent()
}
