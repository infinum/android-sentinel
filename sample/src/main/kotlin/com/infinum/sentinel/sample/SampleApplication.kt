package com.infinum.sentinel.sample

import android.app.Application
import android.os.Build
import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.ui.tools.AppGalleryTool
import com.infinum.sentinel.ui.tools.CertificateTool
import com.infinum.sentinel.ui.tools.ChuckerTool
import com.infinum.sentinel.ui.tools.CollarTool
import com.infinum.sentinel.ui.tools.DbInspectorTool
import com.infinum.sentinel.ui.tools.GooglePlayTool
import com.infinum.sentinel.ui.tools.LeakCanaryTool
import com.infinum.sentinel.ui.tools.NetworkEmulatorTool
import com.infinum.sentinel.ui.tools.ThimbleTool
import com.infinum.sentinel.ui.tools.TimberTool
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

class SampleApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        Sentinel.watch(getWatchedTools(), mapOf("ENCRYPTED_SHARED_PREFERENCES" to emptyList()))

        // Initialize API client for network emulator demo
        ApiClient.initialize(this)
    }

    private fun getWatchedTools(): Set<Sentinel.Tool> {
        val tools = mutableSetOf<Sentinel.Tool>()
        tools.add(ChuckerTool())
        tools.add(CollarTool())
        tools.add(DbInspectorTool())
        tools.add(LeakCanaryTool())
        tools.add(NetworkEmulatorTool())
        tools.add(AppGalleryTool(appId = "102016595"))
        tools.add(GooglePlayTool())
        tools.add(TimberTool(allowedTags = listOf("Main")))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            tools.add(CertificateTool(userCertificates = loadDebugCertificates()))
        }
        return tools
    }

    private fun loadDebugCertificates(): List<X509Certificate> {
        val factory = CertificateFactory.getInstance("X.509")
        return listOf(
            "stackexchange.pem",
            "selfsigned_knobtviker.cert",
        ).map { assets.open(it) }
            .map { factory.generateCertificate(it) as X509Certificate }
    }
}
