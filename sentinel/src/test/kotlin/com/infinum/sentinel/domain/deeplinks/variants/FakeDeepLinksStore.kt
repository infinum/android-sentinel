package com.infinum.sentinel.domain.deeplinks.variants

import com.infinum.sentinel.data.sources.local.preferences.DeepLinksStore

/**
 * In-memory [DeepLinksStore] for tests.
 */
internal class FakeDeepLinksStore : DeepLinksStore {
    var variantsJson: String? = null
    var backup: String? = null
    var writeResult: Boolean = true

    override fun readVariants(): String? = variantsJson

    override fun writeVariants(json: String): Boolean {
        if (writeResult) {
            variantsJson = json
        }
        return writeResult
    }

    override fun backupCorrupt(json: String): Boolean {
        backup = json
        return true
    }
}
