package com.infinum.sentinel.domain.deeplinks

import com.infinum.sentinel.data.models.raw.deeplinks.DomainVerificationData
import com.infinum.sentinel.domain.deeplinks.models.DeepLinksDiscovery
import com.infinum.sentinel.domain.deeplinks.models.Variant
import kotlinx.coroutines.flow.Flow

internal interface DeepLinksRepositories {
    interface DeepLinks {
        suspend fun discover(): DeepLinksDiscovery

        suspend fun verification(): DomainVerificationData?
    }

    interface DeepLinkVariants {
        fun observe(): Flow<List<Variant>>

        suspend fun find(id: String): Variant?

        suspend fun upsert(variant: Variant)

        suspend fun duplicate(
            id: String,
            copySuffix: String,
        ): Variant?

        suspend fun delete(id: String)

        suspend fun deleteAll(): Int
    }
}
