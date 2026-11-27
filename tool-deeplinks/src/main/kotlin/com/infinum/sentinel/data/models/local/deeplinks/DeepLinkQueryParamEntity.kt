package com.infinum.sentinel.data.models.local.deeplinks

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class DeepLinkQueryParamEntity(
    @SerialName("k")
    val key: String,
    @SerialName("v")
    val value: String,
)
