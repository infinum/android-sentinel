package com.infinum.sentinel.data.sources.memory

abstract class AbstractFormattedStringBuilder : FormattedStringBuilder {

    companion object {
        internal const val APPLICATION = "application"
        internal const val DEVICE = "device"
        internal const val PERMISSIONS = "permissions"
        internal const val NAME = "name"
        internal const val STATUS = "status"
    }
}
