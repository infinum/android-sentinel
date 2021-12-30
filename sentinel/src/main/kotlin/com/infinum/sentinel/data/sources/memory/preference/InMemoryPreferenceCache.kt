package com.infinum.sentinel.data.sources.memory.preference

import com.infinum.sentinel.data.models.raw.PreferenceType

internal class InMemoryPreferenceCache : PreferenceCache {

    private var name: String? = null
    private var tuple: Triple<PreferenceType, String, Any>? = null

    override fun save(name: String, tuple: Triple<PreferenceType, String, Any>) {
        this.name = name
        this.tuple = tuple
    }

    override fun load(): Pair<String, Triple<PreferenceType, String, Any>> {
        return this.name!! to this.tuple!!
    }

    override fun clear() {
        this.name = null
        this.tuple = null
    }
}
