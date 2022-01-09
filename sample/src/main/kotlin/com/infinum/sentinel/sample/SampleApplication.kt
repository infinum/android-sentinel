package com.infinum.sentinel.sample

import android.app.Application
import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.ui.tools.AppGalleryTool
import com.infinum.sentinel.ui.tools.CertificateTool
import com.infinum.sentinel.ui.tools.ChuckerTool
import com.infinum.sentinel.ui.tools.CollarTool
import com.infinum.sentinel.ui.tools.DbInspectorTool
import com.infinum.sentinel.ui.tools.GooglePlayTool
import com.infinum.sentinel.ui.tools.LeakCanaryTool
import com.infinum.sentinel.ui.tools.ThimbleTool
import com.infinum.sentinel.ui.tools.TimberTool
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

class SampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Sentinel.watch(
            setOf(
                ChuckerTool(),
                CollarTool(),
                DbInspectorTool(),
                LeakCanaryTool(),
                AppGalleryTool("102016595"),
                GooglePlayTool(),
                ThimbleTool(),
                TimberTool(),
                CertificateTool(
                    loadDebugCertificates()
                )
            )
        )
    }

    private fun loadDebugCertificates(): List<X509Certificate> {
        val factory = CertificateFactory.getInstance("X.509")
        return listOf(
            "stackexchange.pem",
            "selfsigned_knobtviker.com.cert"
        )
            .map { assets.open(it) }
            .map { factory.generateCertificate(it) as X509Certificate }
    }
}
