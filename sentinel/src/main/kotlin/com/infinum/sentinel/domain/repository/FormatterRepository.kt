package com.infinum.sentinel.domain.repository

import android.content.Context
import com.infinum.sentinel.ui.formatters.HtmlStringBuilder
import com.infinum.sentinel.ui.formatters.JsonStringBuilder
import com.infinum.sentinel.ui.formatters.MarkdownStringBuilder
import com.infinum.sentinel.ui.formatters.PlainStringBuilder
import com.infinum.sentinel.ui.formatters.XmlStringBuilder

internal class FormatterRepository(context: Context) {

    private val plainFormatter: PlainStringBuilder = PlainStringBuilder(context)

    private val markdownFormatter: MarkdownStringBuilder = MarkdownStringBuilder(context)

    private val jsonFormatter: JsonStringBuilder = JsonStringBuilder(context)

    private val xmlFormatter: XmlStringBuilder = XmlStringBuilder(context)

    private val htmlFormatter: HtmlStringBuilder = HtmlStringBuilder(context)

    fun plain() = plainFormatter

    fun markdown() = markdownFormatter

    fun json() = jsonFormatter

    fun xml() = xmlFormatter

    fun html() = htmlFormatter
}
