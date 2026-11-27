package com.infinum.sentinel.data.sources.local.deeplinks

/**
 * Raw string storage for the Deep Links tool.
 *
 * All calls are synchronous, so callers run them off the main thread.
 * Every write returns whether it reached disk.
 */
internal interface DeepLinksStore {
    fun readVariants(): String?

    fun writeVariants(json: String): Boolean

    fun backupCorrupt(json: String): Boolean
}
