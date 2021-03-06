package com.infinum.sentinel.sample

data class BundleTree(
    val id: String,
    val size: Int,
    val subTrees: List<BundleTree> = emptyList()
)