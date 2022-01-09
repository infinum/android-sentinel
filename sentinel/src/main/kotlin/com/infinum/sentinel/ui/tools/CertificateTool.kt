package com.infinum.sentinel.ui.tools

import android.content.Intent
import android.view.View
import com.infinum.sentinel.R
import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.ui.certificates.CertificatesActivity
import java.security.cert.X509Certificate

/**
 * Specific wrapper tool that previews X.509 certificates from system and application.
 */
public data class CertificateTool @JvmOverloads constructor(
    val userCertificates: List<X509Certificate> = listOf(),
    private val listener: View.OnClickListener = View.OnClickListener {
        it.context.startActivity(
            Intent(
                it.context,
                CertificatesActivity::class.java
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
        )
    }
) : Sentinel.Tool {

    /**
     * A dedicated name for this tool
     *
     * @return a String resource that will be used to generate a name for a Button in Tools UI
     */
    override fun name(): Int = R.string.sentinel_certificates

    /**
     * A callback to be invoked when this view is clicked.
     *
     * @return an assigned OnClickListener that will be used to generate a Button in Tools UI
     */
    override fun listener(): View.OnClickListener = listener
}
