package com.infinum.sentinel.data.sources.memory

import com.infinum.sentinel.data.sources.raw.DataSource
import org.json.JSONArray
import org.json.JSONObject

class JsonStringBuilder : AbstractFormattedStringBuilder() {

    override fun format(): String =
        JSONObject()
            .put(APPLICATION, JSONObject().apply {
                DataSource.applicationData.forEach {
                    put(it.key.name.toLowerCase(), it.value)
                }
            })
            .put(PERMISSIONS, JSONArray().apply {
                DataSource.permissions.forEach {
                    put(
                        JSONObject()
                            .put(NAME, it.key)
                            .put(STATUS, it.value)
                    )
                }
            })
            .put(DEVICE, JSONObject().apply {
                DataSource.deviceData.forEach {
                    put(it.key.name.toLowerCase(), it.value)
                }
            })
            .toString()
}