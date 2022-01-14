package com.infinum.sentinel.extensions

import android.annotation.SuppressLint
import java.util.Locale
import java.util.regex.Matcher
import java.util.regex.Pattern

@SuppressLint("DefaultLocale")
internal fun String.sanitize() = this.lowercase(Locale.getDefault()).replace(" ", "_")

internal fun String.allOccurrenceIndexes(term: String): ArrayList<Int> {
    val indexes = arrayListOf<Int>()
    var idx = 0
    while (indexOf(term, idx, true).also { idx = it } >= 0) {
        indexes.add(idx)
        idx++
    }
    return indexes
}

internal fun String.asASN(): List<String> {
    val result = mutableListOf<String>()
    val matcher: Matcher = Pattern.compile("(\\w+)=([^,\\n\\r]+)").matcher(this)
    while (matcher.find()) {
        val key = matcher.group(1).orEmpty().trim()
        val value = matcher.group(2).orEmpty().trim()
        result.add("$key = $value")
    }
    return result.toList()
}
