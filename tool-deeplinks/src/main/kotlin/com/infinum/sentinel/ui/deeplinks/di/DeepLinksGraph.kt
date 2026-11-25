package com.infinum.sentinel.ui.deeplinks.di

import android.content.Context
import android.os.Build
import com.infinum.sentinel.data.sources.local.deeplinks.SharedPreferencesDeepLinksStore
import com.infinum.sentinel.data.sources.raw.deeplinks.ComponentStatesCollector
import com.infinum.sentinel.data.sources.raw.deeplinks.DeepLinksCollector
import com.infinum.sentinel.data.sources.raw.deeplinks.DomainVerificationCollector
import com.infinum.sentinel.data.sources.raw.deeplinks.ManifestReader
import com.infinum.sentinel.domain.deeplinks.DeepLinksRepositories
import com.infinum.sentinel.domain.deeplinks.DeepLinksRepository
import com.infinum.sentinel.domain.deeplinks.ExampleLinksRegistry
import com.infinum.sentinel.domain.deeplinks.variants.DeepLinkVariantsRepository

/**
 * The Deep links tool's dependencies, wired by hand. [init] must run before the first repository is read, which
 * `DeepLinksActivity` does in `onCreate`.
 *
 * The two repositories live for the whole process: the list and the editor share the discovery cache and the
 * variants flow.
 */
internal object DeepLinksGraph {
    @Volatile
    private var appContext: Context? = null

    private val context: Context
        get() = checkNotNull(appContext) { "DeepLinksGraph.init must run before the Deep links screens." }

    /** Keeps the application context of [context]. Later calls change nothing. */
    fun init(context: Context) {
        if (appContext == null) appContext = context.applicationContext
    }

    val deepLinks: DeepLinksRepositories.DeepLinks by lazy {
        DeepLinksRepository(
            collector = DeepLinksCollector(ManifestReader(context), ExampleLinksRegistry.links),
            verificationCollector = DomainVerificationCollector(context),
            packageName = context.packageName,
            sdkInt = Build.VERSION.SDK_INT,
            componentStates = ComponentStatesCollector(context),
        )
    }

    val deepLinkVariants: DeepLinksRepositories.DeepLinkVariants by lazy {
        DeepLinkVariantsRepository(SharedPreferencesDeepLinksStore(context))
    }
}
