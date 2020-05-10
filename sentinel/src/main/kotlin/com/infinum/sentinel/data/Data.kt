package com.infinum.sentinel.data

import android.content.Context
import com.infinum.sentinel.data.models.memory.CacheProvider
import com.infinum.sentinel.data.sources.local.DatabaseProvider

internal object Data {

    private lateinit var context: Context

    private lateinit var databaseProvider: DatabaseProvider

    private lateinit var cacheProvider: CacheProvider

    fun initialise(context: Context) {
        this.context = context
        this.databaseProvider = DatabaseProvider.initialise(this.context)
        this.cacheProvider = CacheProvider.initialise(this.context)
    }

    fun setup(onTriggered: () -> Unit) {
        CacheProvider.setup(onTriggered)
    }

    fun database() = databaseProvider

    fun cache() = cacheProvider
}
