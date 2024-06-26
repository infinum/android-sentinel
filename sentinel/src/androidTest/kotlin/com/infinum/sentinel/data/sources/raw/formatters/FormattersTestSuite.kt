package com.infinum.sentinel.data.sources.raw.formatters

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
public class FormattersTestSuite
