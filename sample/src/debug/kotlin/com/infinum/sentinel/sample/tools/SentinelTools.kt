package com.infinum.sentinel.sample.tools

import com.infinum.sentinel.ui.tools.ChuckerTool
import com.infinum.sentinel.ui.tools.CollarTool
import com.infinum.sentinel.ui.tools.DbInspectorTool
import com.infinum.sentinel.ui.tools.GooglePlayTool

object SentinelTools {

    fun get() = listOf(
        ChuckerTool(),
        CollarTool(),
        DbInspectorTool(),
        GooglePlayTool()
    )
}