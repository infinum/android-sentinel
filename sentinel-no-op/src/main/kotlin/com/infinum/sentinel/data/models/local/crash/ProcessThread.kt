package com.infinum.sentinel.data.models.local.crash

internal data class ProcessThread(

    val name: String,

    val state: String,

    val stackTrace: List<String>
)
