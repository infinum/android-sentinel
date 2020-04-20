package com.infinum.sentinel.data.sources.memory

import android.util.Xml
import com.infinum.sentinel.data.sources.raw.DataSource
import java.io.StringWriter

class XmlStringBuilder : AbstractFormattedStringBuilder() {

    companion object {
        private const val NAMESPACE = "sentinel"
        private const val ROOT = "sentinel"
        private const val PERMISSION = "permission"
    }

    override fun format(): String =
        StringWriter().apply {
            with(Xml.newSerializer()) {
                setOutput(this@apply)
                startDocument(Charsets.UTF_8.name().toUpperCase(), true)

                startTag(NAMESPACE, ROOT)

                startTag(NAMESPACE, APPLICATION)
                DataSource.applicationData.forEach {
                    startTag(NAMESPACE, it.key.name.toLowerCase())
                    text(it.value)
                    endTag(NAMESPACE, it.key.name.toLowerCase())
                }
                endTag(NAMESPACE, APPLICATION)

                startTag(NAMESPACE, PERMISSIONS)
                DataSource.permissions.forEach {
                    startTag(NAMESPACE, PERMISSION)
                    attribute(NAMESPACE, NAME, it.key)
                    attribute(NAMESPACE, STATUS, it.value.toString())
                    endTag(NAMESPACE, PERMISSION)
                }
                endTag(NAMESPACE, PERMISSIONS)

                startTag(NAMESPACE, DEVICE)
                DataSource.deviceData.forEach {
                    startTag(NAMESPACE, it.key.name.toLowerCase())
                    text(it.value)
                    endTag(NAMESPACE, it.key.name.toLowerCase())
                }
                endTag(NAMESPACE, DEVICE)

                endTag(NAMESPACE, ROOT)

                endDocument()
            }
        }.toString()
}
