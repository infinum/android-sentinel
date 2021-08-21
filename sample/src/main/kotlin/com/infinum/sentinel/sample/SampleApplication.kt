package com.infinum.sentinel.sample

import android.app.Application
import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.ui.tools.AppGalleryTool
import com.infinum.sentinel.ui.tools.ChuckerTool
import com.infinum.sentinel.ui.tools.CollarTool
import com.infinum.sentinel.ui.tools.DbInspectorTool
import com.infinum.sentinel.ui.tools.GooglePlayTool
import com.infinum.sentinel.ui.tools.LeakCanaryTool
import com.infinum.sentinel.ui.tools.ThimbleTool

class SampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Sentinel.watch(
            setOf(
                ChuckerTool(),
                CollarTool(),
                DbInspectorTool(),
                LeakCanaryTool(),
                AppGalleryTool(appId = "102016595"),
                GooglePlayTool(),
                ThimbleTool(),
            )
        )
    }
}
