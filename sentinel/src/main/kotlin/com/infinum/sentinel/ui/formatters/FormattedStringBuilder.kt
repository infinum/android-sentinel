package com.infinum.sentinel.ui.formatters

internal interface FormattedStringBuilder<T, K> {

    fun format(): String

    fun application(): T

    fun permissions(): K

    fun device(): T

    fun preferences(): K
}
