package com.infinum.sentinel.ui.tools

import android.view.View
import com.infinum.sentinel.Sentinel
import java.security.cert.X509Certificate

/**
 * Specific wrapper tool that previews X.509 certificates from system and application.
 */
@Suppress("UnusedPrivateMember")
public data class CertificateTool
@JvmOverloads
constructor(
    private val userCertificates: List<X509Certificate> = listOf(),
    private val listener: View.OnClickListener =
        View.OnClickListener {
            // no - op
        },
) : Sentinel.Tool {
    override fun name(): Int = 0

    override fun listener(): View.OnClickListener =
        View.OnClickListener {
            // no - op
        }
}
