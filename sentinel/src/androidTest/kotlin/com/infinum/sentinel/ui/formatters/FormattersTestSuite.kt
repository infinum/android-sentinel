package com.infinum.sentinel.ui.formatters

import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.junit.runners.Suite.SuiteClasses

@RunWith(Suite::class)
@SuiteClasses(
    PlainStringBuilderTests::class,
    MarkdownStringBuilderTests::class,
    JsonStringBuilderTests::class,
    XmlStringBuilderTests::class,
    HtmlStringBuilderTests::class
)
class FormattersTestSuite