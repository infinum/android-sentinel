package com.infinum.sentinel.domain.repository

import android.content.Context
import com.infinum.sentinel.ui.formatters.HtmlStringBuilder
import com.infinum.sentinel.ui.formatters.JsonStringBuilder
import com.infinum.sentinel.ui.formatters.MarkdownStringBuilder
import com.infinum.sentinel.ui.formatters.PlainStringBuilder
import com.infinum.sentinel.ui.formatters.XmlStringBuilder

internal object FormatterRepository {

    private lateinit var plainFormatter: PlainStringBuilder
    private lateinit var markdownFormatter: MarkdownStringBuilder
    private lateinit var jsonFormatter: JsonStringBuilder
    private lateinit var xmlFormatter: XmlStringBuilder
    private lateinit var htmlFormatter: HtmlStringBuilder

    fun initialise(context: Context) {
        plainFormatter = PlainStringBuilder(context)
        markdownFormatter = MarkdownStringBuilder(context)
        jsonFormatter = JsonStringBuilder(context)
        xmlFormatter = XmlStringBuilder(context)
        htmlFormatter = HtmlStringBuilder(context)
    }

    fun plain() = plainFormatter

    fun markdown() = markdownFormatter

    fun json() = jsonFormatter

    fun xml() = xmlFormatter

    fun html() = htmlFormatter
}
