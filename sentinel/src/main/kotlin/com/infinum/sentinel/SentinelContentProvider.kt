package com.infinum.sentinel

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import com.infinum.sentinel.ui.DependencyGraph

class SentinelContentProvider : ContentProvider() {

    override fun onCreate(): Boolean =
        context?.let {
            DependencyGraph.initialise(it.applicationContext)
            true
        } ?: false

    override fun insert(
        uri: Uri,
        values: ContentValues?
    ): Uri? = null

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? = null

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = 0

    override fun getType(uri: Uri): String? = null
}
