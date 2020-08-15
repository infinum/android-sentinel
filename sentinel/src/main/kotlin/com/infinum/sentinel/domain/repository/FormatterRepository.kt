package com.infinum.sentinel.domain.repository

import android.content.Context
import com.infinum.sentinel.ui.formatters.HtmlStringBuilder
import com.infinum.sentinel.ui.formatters.JsonStringBuilder
import com.infinum.sentinel.ui.formatters.MarkdownStringBuilder
import com.infinum.sentinel.ui.formatters.PlainStringBuilder
import com.infinum.sentinel.ui.formatters.XmlStringBuilder

internal class FormatterRepository(context: Context) {

    val plain: PlainStringBuilder = PlainStringBuilder(context)

    val markdown: MarkdownStringBuilder = MarkdownStringBuilder(context)

    val json: JsonStringBuilder = JsonStringBuilder(context)

    val xml: XmlStringBuilder = XmlStringBuilder(context)

    val html: HtmlStringBuilder = HtmlStringBuilder(context)
}
