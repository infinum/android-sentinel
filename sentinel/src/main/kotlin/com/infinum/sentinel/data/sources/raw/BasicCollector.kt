package com.infinum.sentinel.data.sources.raw

import android.content.Context
import com.infinum.sentinel.data.models.raw.BasicData

internal class BasicCollector(
    private val context: Context
) : AbstractCollector<BasicData>() {

    override lateinit var data: BasicData

    override fun collect() {
        with(context) {
            data = BasicData(
                applicationIcon = applicationInfo.loadIcon(packageManager),
                applicationName = applicationInfo.loadLabel(packageManager).toString()
            )
        }
    }
}
