package com.infinum.sentinel.domain.deeplinks

import android.util.Log
import com.infinum.sentinel.data.models.raw.deeplinks.DeepLinksData
import com.infinum.sentinel.data.models.raw.deeplinks.DomainVerificationData
import com.infinum.sentinel.data.models.raw.deeplinks.ManifestReadResult
import com.infinum.sentinel.domain.Repositories
import com.infinum.sentinel.domain.collectors.Collectors
import com.infinum.sentinel.domain.deeplinks.discovery.BaseLinkExpander
import com.infinum.sentinel.domain.deeplinks.discovery.ConfigurationWarnings
import com.infinum.sentinel.domain.deeplinks.discovery.ExampleLinkMapper
import com.infinum.sentinel.domain.deeplinks.discovery.ManifestFilterExtractor
import com.infinum.sentinel.domain.deeplinks.models.DeclaredFilter
import com.infinum.sentinel.domain.deeplinks.models.DeclaredScreen
import com.infinum.sentinel.domain.deeplinks.models.DeepLinksDiscovery
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

/**
 * Discovers deep links once and keeps the result for the process lifetime.
 *
 * Discovery runs under a mutex on [ioDispatcher], the collector is invoked at most once and
 * [discover] never throws. Construction does no work.
 */
internal class DeepLinksRepository(
    private val collector: Collectors.DeepLinks,
    private val verificationCollector: Collectors.DomainVerification,
    private val packageName: String,
    private val sdkInt: Int,
    private val componentStates: Collectors.ComponentStates = Collectors.ComponentStates { null },
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : Repositories.DeepLinks {
    private companion object {
        const val TAG = "Sentinel"
    }

    private val mutex = Mutex()
    private var cached: DeepLinksDiscovery? = null

    override suspend fun discover(): DeepLinksDiscovery =
        mutex.withLock {
            cached ?: withContext(ioDispatcher) { compute() }.also { cached = it }
        }

    override suspend fun verification(): DomainVerificationData? =
        withContext(ioDispatcher) {
            try {
                verificationCollector()
            } catch (e: Exception) {
                warn("Reading the App Links verification state failed", e)
                null
            }
        }

    private fun compute(): DeepLinksDiscovery =
        try {
            build(collector())
        } catch (e: Exception) {
            warn("Deep link discovery failed", e)
            DeepLinksDiscovery(examples = listOf(), screens = listOf(), isDiscoveryFailed = true)
        }

    private fun build(data: DeepLinksData): DeepLinksDiscovery {
        val examples = ExampleLinkMapper.map(data.examples)
        val screens =
            when (val manifest = data.manifest) {
                is ManifestReadResult.Success -> declaredScreens(manifest)
                is ManifestReadResult.Failure -> {
                    warn("Reading the manifest failed", manifest.cause)
                    null
                }
            }
        return DeepLinksDiscovery(
            examples = examples,
            screens = screens.orEmpty(),
            isDiscoveryFailed = screens == null,
        )
    }

    /**
     * [filters] with the enabled state the app set at runtime, where it set one. Discovery runs once per process,
     * so a component the app enables or disables later keeps the state it had then.
     */
    private fun withRuntimeState(filters: List<DeclaredFilter>): List<DeclaredFilter> {
        val runtime = filters.map { it.componentName }.distinct().associateWith { componentStates.runtimeEnabled(it) }
        return filters.map { filter -> runtime[filter.componentName]?.let { filter.copy(isEnabled = it) } ?: filter }
    }

    private fun declaredScreens(manifest: ManifestReadResult.Success): List<DeclaredScreen>? =
        try {
            val filters = withRuntimeState(ManifestFilterExtractor.extract(manifest.root, packageName))
            ConfigurationWarnings.attachDuplicates(BaseLinkExpander.expand(filters, sdkInt))
        } catch (e: Exception) {
            warn("Expanding the manifest filters failed", e)
            null
        }

    /**
     * Logs a dropped failure. Logging must never break discovery, and the framework logger can throw where
     * it is only a stub, such as in local unit tests, so a failure to log is ignored.
     */
    private fun warn(
        message: String,
        cause: Throwable,
    ) {
        try {
            Log.w(TAG, message, cause)
        } catch (ignored: RuntimeException) {
            // Nothing else to do: the failure being logged is already handled by the caller.
        }
    }
}
