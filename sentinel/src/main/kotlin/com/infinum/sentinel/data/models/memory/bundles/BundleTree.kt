package com.infinum.sentinel.data.models.memory.bundles

internal data class BundleTree(
    val id: String,
    val size: Int,
    val subTrees: List<BundleTree> = emptyList()
)