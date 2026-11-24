package com.infinum.sentinel.domain.deeplinks.variants

import com.infinum.sentinel.data.sources.local.preferences.DeepLinksStore
import com.infinum.sentinel.domain.Repositories
import com.infinum.sentinel.domain.deeplinks.models.Variant
import java.util.UUID
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Keeps tester-made variants in a [DeepLinksStore].
 *
 * Storage is read once, on first use. Every mutation runs under a mutex, writes to storage first
 * and updates the observed state only when the write succeeded. An unreadable stored document is backed up
 * before any write can replace it.
 */
internal class DeepLinkVariantsRepository(
    private val store: DeepLinksStore,
    private val idFactory: () -> String = { UUID.randomUUID().toString() },
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : Repositories.DeepLinkVariants {
    // Null means the storage has not been read yet.
    private val state = MutableStateFlow<List<Variant>?>(null)
    private val mutex = Mutex()

    // An unreadable stored document whose backup failed. Guarded by [mutex]; the next write retries the backup.
    private var pendingBackup: String? = null

    override fun observe(): Flow<List<Variant>> =
        flow {
            mutex.withLock { loadLocked() }
            emitAll(state.filterNotNull())
        }

    override suspend fun find(id: String): Variant? =
        mutex.withLock { loadLocked().firstOrNull { it.id == id } }

    override suspend fun upsert(variant: Variant) {
        mutex.withLock {
            val current = loadLocked()
            val index = current.indexOfFirst { it.id == variant.id }
            val updated =
                if (index >= 0) {
                    current.toMutableList().also { it[index] = variant }
                } else {
                    current + variant
                }
            persist(updated)
        }
    }

    override suspend fun duplicate(
        id: String,
        copySuffix: String,
    ): Variant? =
        mutex.withLock {
            val current = loadLocked()
            val index = current.indexOfFirst { it.id == id }
            if (index < 0) {
                null
            } else {
                val original = current[index]
                val copy = original.copy(
                    id = idFactory(),
                    name = original.name?.let { "$it $copySuffix" },
                )
                persist(current.toMutableList().also { it.add(index + 1, copy) })
                copy
            }
        }

    override suspend fun delete(id: String) {
        mutex.withLock {
            val current = loadLocked()
            if (current.any { it.id == id }) {
                persist(current.filterNot { it.id == id })
            }
        }
    }

    override suspend fun deleteAll(): Int =
        mutex.withLock {
            val current = loadLocked()
            if (current.isEmpty()) {
                0
            } else {
                persist(emptyList())
                current.size
            }
        }

    /**
     * Must be called with [mutex] held. Reads storage on first call and never throws.
     */
    private suspend fun loadLocked(): List<Variant> =
        state.value ?: readFromStore().also { state.value = it }

    private suspend fun readFromStore(): List<Variant> {
        val raw = withContext(ioDispatcher) { store.readVariants() }
        val decoded = raw?.let { DeepLinkVariantsSerializer.decode(it) }
        if (raw != null && decoded == null) {
            val isBackedUp = withContext(ioDispatcher) { store.backupCorrupt(raw) }
            if (!isBackedUp) pendingBackup = raw
        }
        return decoded ?: emptyList()
    }

    /**
     * Persists [list] and only then publishes it. Throws [VariantsWriteException] when the write fails, or when
     * an unreadable stored document still can't be backed up, so a write never replaces its only copy.
     *
     * The write and the publish can't be cancelled: once storage holds [list], the observed state must hold it
     * too, or the next mutation would build on the old list and write it back.
     */
    private suspend fun persist(list: List<Variant>) {
        val json = DeepLinkVariantsSerializer.encode(list)
        withContext(ioDispatcher + NonCancellable) {
            backUpPending()
            if (!store.writeVariants(json)) throw VariantsWriteException()
            state.value = list
        }
    }

    /**
     * Must be called with [mutex] held. Retries a failed backup and throws [VariantsWriteException] if it fails again.
     */
    private fun backUpPending() {
        val raw = pendingBackup ?: return
        if (!store.backupCorrupt(raw)) throw VariantsWriteException()
        pendingBackup = null
    }
}
