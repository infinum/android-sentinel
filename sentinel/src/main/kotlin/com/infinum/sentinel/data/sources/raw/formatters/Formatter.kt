package com.infinum.sentinel.data.sources.raw.formatters

import com.infinum.sentinel.data.models.local.CrashEntity

internal interface Formatter<Model, Collection> {
    companion object {
        internal const val APPLICATION = "application"
        internal const val DEVICE = "device"
        internal const val PERMISSIONS = "permissions"
        internal const val NAME = "name"
        internal const val STATUS = "status"
        internal const val PREFERENCES = "preferences"
        internal const val CRASH = "crash"
        internal const val VALUES = "values"
    }

    operator fun invoke(): String

    fun formatCrash(
        includeAllData: Boolean,
        entity: CrashEntity,
    ): String

    fun application(): Model

    fun permissions(): Collection

    fun device(): Model

    fun preferences(): Collection

    fun crash(entity: CrashEntity): Model
}
