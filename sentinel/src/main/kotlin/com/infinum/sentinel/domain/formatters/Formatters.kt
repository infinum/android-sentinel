package com.infinum.sentinel.domain.formatters

import com.infinum.sentinel.data.sources.raw.formatters.Formatter
import org.json.JSONArray
import org.json.JSONObject

internal interface Formatters {

    interface Plain : Formatter<String, String>

    interface Markdown : Formatter<String, String>

    interface Json : Formatter<JSONObject, JSONArray>

    interface Xml : Formatter<String, String>

    interface Html : Formatter<String, String>
}
