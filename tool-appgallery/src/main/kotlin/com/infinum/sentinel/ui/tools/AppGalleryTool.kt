package com.infinum.sentinel.ui.tools

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.view.View
import com.infinum.sentinel.R
import com.infinum.sentinel.Sentinel

/**
 * Specific wrapper tool around Huawei AppGallery.
 *
 * @param appId is your Huawei application ID from Huawei developer console.
 *
 * Tool Activity will launch with FLAG_ACTIVITY_RESET_TASK_IF_NEEDED, FLAG_ACTIVITY_CLEAR_TOP
 * and FLAG_ACTIVITY_NEW_TASK flags.
 * If no appropriate application is found, this tool will open a website on appgallery.cloud.huawei.com.
 */
@SuppressLint("QueryPermissionsNeeded")
public data class AppGalleryTool @JvmOverloads constructor(
    private val appId: String,
    private val listener: View.OnClickListener = View.OnClickListener { view ->
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
            .let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    it.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0L))
                } else {
                    @Suppress("DEPRECATION")
                    it.queryIntentActivities(intent, 0)
                }
            }
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
            } ?: view.context.startActivity(
            Intent(
                Intent.ACTION_VIEW,
                Uri.Builder()
                    .scheme(SCHEME_HTTPS)
                    .authority(AUTHORITY)
                    .appendPath(PATH_MARKETSHARE)
                    .appendPath(PATH_APP)
                    .appendPath("${PATH_C}$appId")
                    .build()
            )
        )
    }
) : Sentinel.Tool {

    internal companion object {
        private const val SCHEME_MARKET = "appmarket"
        private const val PATH_DETAILS = "details"
        private const val QUERY_ID = "id"
        private const val VENDING_PACKAGE_NAME = "com.huawei.appmarket"

        private const val SCHEME_HTTPS = "https"
        private const val AUTHORITY = "appgallery.cloud.huawei.com"
        private const val PATH_MARKETSHARE = "marketshare"
        private const val PATH_APP = "app"
        private const val PATH_C = "C"
    }

    /**
     * An optional icon for this tool
     *
     * @return a Drawable resource that will be used to generate an icon in a Button in Tools UI
     */
    override fun icon(): Int? = null

    /**
     * A dedicated name for this tool
     *
     * @return a String resource that will be used to generate a name for a Button in Tools UI
     */
    override fun name(): Int = R.string.sentinel_app_gallery

    /**
     * A callback to be invoked when this view is clicked.
     *
     * @return an assigned OnClickListener that will be used to generate a Button in Tools UI
     */
    override fun listener(): View.OnClickListener = listener
}
