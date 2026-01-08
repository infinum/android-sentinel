package com.infinum.sentinel.data.models.local.crash

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class CrashData(
    @SerialName("thread")
    val thread: ThreadData? = null,
    @SerialName("exception")
    val exception: ExceptionData? = null,
    @SerialName("thread_state")
    val threadState: List<ProcessThread>? = null,
)
