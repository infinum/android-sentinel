package com.infinum.sentinel.data

import android.content.Context
import com.infinum.sentinel.data.models.memory.CacheProvider
import com.infinum.sentinel.data.sources.local.DatabaseProvider

internal object Data {

    private lateinit var databaseProvider: DatabaseProvider

    private lateinit var cacheProvider: CacheProvider

    fun initialise(context: Context, onTriggered: () -> Unit) {
        databaseProvider = DatabaseProvider.initialise(context)
        cacheProvider = CacheProvider.initialise(context, onTriggered)
    }

    fun database() = databaseProvider
    fun cache() = cacheProvider
}
