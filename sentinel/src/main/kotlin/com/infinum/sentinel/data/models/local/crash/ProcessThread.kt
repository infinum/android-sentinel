package com.infinum.sentinel.data.models.local.crash

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ProcessThread(
    @SerialName("name")
    val name: String,
    @SerialName("state")
    val state: String,
    @SerialName("stack_trace")
    val stackTrace: List<String>,
)
