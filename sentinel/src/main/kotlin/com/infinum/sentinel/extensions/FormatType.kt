package com.infinum.sentinel.extensions

import com.infinum.sentinel.data.models.memory.formats.FormatType
import com.infinum.sentinel.domain.Factories

internal fun FormatType.formatter(formatters: Factories.Formatter) =
    when (this) {
        FormatType.PLAIN -> formatters.plain()
        FormatType.MARKDOWN -> formatters.markdown()
        FormatType.JSON -> formatters.json()
        FormatType.XML -> formatters.xml()
        FormatType.HTML -> formatters.html()
    }
