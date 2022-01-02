package com.infinum.sentinel.extensions

import android.annotation.SuppressLint
import java.util.Locale

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
