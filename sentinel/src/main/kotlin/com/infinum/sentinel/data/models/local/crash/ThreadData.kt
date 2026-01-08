package com.infinum.sentinel.data.models.local.crash

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ThreadData(
    @SerialName("id")
    val id: Long,
    @SerialName("name")
    val name: String,
    @SerialName("priority")
    val priority: Int,
    @SerialName("is_daemon")
    val isDaemon: Boolean,
    @SerialName("state")
    val state: String,
    @SerialName("group")
    val group: ThreadGroupData?,
)
