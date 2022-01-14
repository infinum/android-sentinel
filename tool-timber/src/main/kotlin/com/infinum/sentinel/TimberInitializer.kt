package com.infinum.sentinel

import android.content.Context
import androidx.startup.Initializer
import com.infinum.sentinel.ui.logger.models.FlowBuffer
import kotlinx.coroutines.MainScope
import timber.log.Timber

internal class TimberInitializer : Initializer<Class<TimberInitializer>> {

    override fun create(context: Context): Class<TimberInitializer> {

        Timber.plant(
            SentinelTree(
                FlowBuffer()
            )
        )

        return TimberInitializer::class.java
    }

    override fun dependencies(): List<Class<out Initializer<*>>> =
        listOf()
}
