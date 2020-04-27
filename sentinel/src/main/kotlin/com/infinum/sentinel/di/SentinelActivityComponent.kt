package com.infinum.sentinel.di

import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.data.sources.raw.ApplicationCollector
import com.infinum.sentinel.data.sources.raw.BasicCollector
import com.infinum.sentinel.data.sources.raw.DeviceCollector
import com.infinum.sentinel.data.sources.raw.PermissionsCollector
import com.infinum.sentinel.data.sources.raw.PreferencesCollector
import com.infinum.sentinel.data.sources.raw.ToolsCollector
import com.infinum.sentinel.domain.repository.FormatsRepository
import com.infinum.sentinel.ui.formatters.HtmlStringBuilder
import com.infinum.sentinel.ui.formatters.JsonStringBuilder
import com.infinum.sentinel.ui.formatters.MarkdownStringBuilder
import com.infinum.sentinel.ui.formatters.PlainStringBuilder
import com.infinum.sentinel.ui.formatters.XmlStringBuilder
import org.koin.dsl.module

object SentinelActivityComponent {

    fun modules() = listOf(
        repositories(),
        formatters()
    )

    private fun repositories() =
        module {
            single {
                FormatsRepository(get(), get())
            }
        }

    private fun formatters() =
        module {
            single {
                PlainStringBuilder(get(), get(), get(), get(), get())
            }
            single {
                MarkdownStringBuilder(get(), get(), get(), get(), get())
            }
            single {
                JsonStringBuilder(get(), get(), get(), get(), get())
            }
            single {
                XmlStringBuilder(get(), get(), get(), get(), get())
            }
            single {
                HtmlStringBuilder(get(), get(), get(), get(), get())
            }
        }
}