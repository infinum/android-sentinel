package com.infinum.sentinel.domain

import com.infinum.sentinel.domain.collectors.Collectors
import com.infinum.sentinel.domain.formatters.Formatters

internal interface Factories {

    interface Collector {

        fun device(): Collectors.Device

        fun application(): Collectors.Application

        fun permissions(): Collectors.Permissions

        fun preferences(): Collectors.Preferences

        fun tools(): Collectors.Tools
    }

    interface Formatter {

        fun plain(): Formatters.Plain

        fun markdown(): Formatters.Markdown

        fun json(): Formatters.Json

        fun xml(): Formatters.Xml

        fun html(): Formatters.Html
    }
}