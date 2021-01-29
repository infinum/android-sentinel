package com.infinum.sentinel.data.sources.raw.formatters

internal interface Formatter<T, K> {

    companion object {
        internal const val APPLICATION = "application"
        internal const val DEVICE = "device"
        internal const val PERMISSIONS = "permissions"
        internal const val NAME = "name"
        internal const val STATUS = "status"
        internal const val PREFERENCES = "preferences"
        internal const val VALUES = "values"
    }

    operator fun invoke(): String

    fun application(): T

    fun permissions(): K

    fun device(): T

    fun preferences(): K
}
