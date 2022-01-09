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
                CertificateTool(listOf())
            )
        )
    }
}
