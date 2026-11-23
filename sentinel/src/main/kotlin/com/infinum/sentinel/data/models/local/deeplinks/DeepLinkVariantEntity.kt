package com.infinum.sentinel.data.models.local.deeplinks

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class DeepLinkVariantEntity(
    val id: String,
    val name: String? = null,
    val baseKey: String,
    @SerialName("component")
    val componentName: String? = null,
    val scheme: String,
    @SerialName("authority")
    val hasAuthority: Boolean,
    val host: String? = null,
    val port: Int? = null,
    val path: String,
    val fragment: String? = null,
    val params: List<DeepLinkQueryParamEntity> = listOf(),
)
