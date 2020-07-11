package com.infinum.sentinel.sample

import android.app.Application
import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.ui.tools.ChuckerTool
import com.infinum.sentinel.ui.tools.CollarTool
import com.infinum.sentinel.ui.tools.DbInspectorTool
import com.infinum.sentinel.ui.tools.DesignerTool
import com.infinum.sentinel.ui.tools.GooglePlayTool

class SampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Sentinel.watch(
            setOf(
                ChuckerTool(),
                CollarTool(),
                DbInspectorTool(),
                GooglePlayTool(),
                DesignerTool()
            )
        )
    }
}