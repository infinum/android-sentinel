package com.infinum.sentinel.ui.formatters

internal interface FormattedStringBuilder {

    fun format(): String

    fun application(): String

    fun permissions(): String

    fun device(): String

    fun preferences(): String
}
