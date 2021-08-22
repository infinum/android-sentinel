package com.infinum.sentinel.domain.formatters

import com.infinum.sentinel.domain.Factories

internal class FormatterFactory(
    private val plain: Formatters.Plain,
    private val markdown: Formatters.Markdown,
    private val json: Formatters.Json,
    private val xml: Formatters.Xml,
    private val html: Formatters.Html
) : Factories.Formatter {

    override fun plain(): Formatters.Plain = plain

    override fun markdown(): Formatters.Markdown = markdown

    override fun json(): Formatters.Json = json

    override fun xml(): Formatters.Xml = xml

    override fun html(): Formatters.Html = html
}
