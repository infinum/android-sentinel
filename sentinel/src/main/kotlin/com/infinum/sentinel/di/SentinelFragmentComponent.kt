package com.infinum.sentinel.di

import com.infinum.sentinel.domain.repository.FormatsRepository
import com.infinum.sentinel.ui.formatters.HtmlStringBuilder
import com.infinum.sentinel.ui.formatters.JsonStringBuilder
import com.infinum.sentinel.ui.formatters.MarkdownStringBuilder
import com.infinum.sentinel.ui.formatters.PlainStringBuilder
import com.infinum.sentinel.ui.formatters.XmlStringBuilder
import org.koin.dsl.module

object SentinelFragmentComponent {

    fun modules() =  module {
        single{
            FormatsRepository(get(), get())
        }
        single {
            PlainStringBuilder(get())
        }
        single {
            MarkdownStringBuilder(get())
        }
        single {
            JsonStringBuilder(get())
        }
        single {
            XmlStringBuilder(get())
        }
        single {
            HtmlStringBuilder(get())
        }
    }
}