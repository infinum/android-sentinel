package com.infinum.sentinel

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.net.Uri
import com.infinum.sentinel.ui.DependencyGraph

internal class SentinelContentProvider : ContentProvider() {

    companion object {
        private const val DEFAULT_PACKAGE = "com.infinum.sentinel"
    }

    override fun attachInfo(context: Context?, info: ProviderInfo?) {
        info?.let {
            if ("$DEFAULT_PACKAGE.${SentinelContentProvider::class.java.simpleName}" == it.authority) {
                throw IllegalStateException(
                    "Incorrect provider authority. " +
                            "Most likely due to missing applicationId variable in module build.gradle."
                )
            }
        } ?: throw IllegalStateException("This component cannot work with null ProviderInfo.")

        super.attachInfo(context, info)
    }

    override fun onCreate(): Boolean =
        context?.let {
            DependencyGraph.initialise(it.applicationContext)
            true
        } ?: false

    override fun insert(
        uri: Uri,
        values: ContentValues?
    ): Uri? = throw NotImplementedError()

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? = throw NotImplementedError()

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = throw NotImplementedError()

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int = throw NotImplementedError()

    override fun getType(uri: Uri): String? = throw NotImplementedError()
}
