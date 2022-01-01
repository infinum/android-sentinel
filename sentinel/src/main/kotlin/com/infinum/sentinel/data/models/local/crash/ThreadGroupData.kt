package com.infinum.sentinel.data.models.local.crash

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ThreadGroupData(

    @SerialName("name")
    val name: String,

    @SerialName("parent")
    val parent: String,

    @SerialName("active_count")
    val activeCount: Int
)
