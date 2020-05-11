package com.infinum.sentinel.data

import android.content.Context
import com.infinum.sentinel.data.models.memory.CacheProvider
import com.infinum.sentinel.data.sources.local.DatabaseProvider

internal class DataGraph(context: Context) {

    private val databaseProvider: DatabaseProvider = DatabaseProvider(context)

    private val cacheProvider: CacheProvider = CacheProvider(context)

    fun setup(onTriggered: () -> Unit) = cacheProvider.setup(onTriggered)

    fun database() = databaseProvider

    fun cache() = cacheProvider
}
