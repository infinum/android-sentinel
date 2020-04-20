package com.infinum.sentinel.ui.tools

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.view.View
import com.infinum.sentinel.Sentinel

class GooglePlayTool : Sentinel.DistributionTool {

    companion object {
        private const val SCHEME_MARKET = "market"
        private const val SCHEME_HTTPS = "https"
        private const val AUTHORITY_GOOGLE_PLAY = "play.google.com"
        private const val PATH_STORE = "store"
        private const val PATH_APPS = "apps"
        private const val PATH_DETAILS = "details"
        private const val QUERY_ID = "id"

        private const val VENDING_PACKAGE_NAME = "com.android.vending"
    }

    override fun listener(): View.OnClickListener = View.OnClickListener { view ->
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.Builder()
                .scheme(SCHEME_MARKET)
                .appendPath(PATH_DETAILS)
                .appendQueryParameter(QUERY_ID, view.context.packageName)
                .build()
        )
        view.context
            .packageManager
            .queryIntentActivities(intent, 0)
            .toList()
            .find {
                it.activityInfo
                    .applicationInfo
                    .packageName
                    .equals(VENDING_PACKAGE_NAME, true)
            }
            ?.let {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.addFlags(Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                intent.component = ComponentName(
                    it.activityInfo.applicationInfo.packageName,
                    it.activityInfo.name
                )
                view.context.startActivity(intent)
            } ?: run {
            view.context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.Builder()
                        .scheme(SCHEME_HTTPS)
                        .authority(AUTHORITY_GOOGLE_PLAY)
                        .appendPath(PATH_STORE)
                        .appendPath(PATH_APPS)
                        .appendPath(PATH_DETAILS)
                        .appendQueryParameter(QUERY_ID, view.context.packageName)
                        .build()
                )
            )
        }
    }
}
