package com.infinum.sentinel.ui.formatters

internal abstract class AbstractFormattedStringBuilder : FormattedStringBuilder {

    companion object {
        internal const val APPLICATION = "application"
        internal const val DEVICE = "device"
        internal const val PERMISSIONS = "permissions"
        internal const val NAME = "name"
        internal const val STATUS = "status"
        internal const val PREFERENCES = "preferences"
        internal const val VALUES = "values"
    }
}
