package com.infinum.sentinel.ui.tools

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.View
import com.infinum.sentinel.R
import com.infinum.sentinel.Sentinel

class AppInfoTool : Sentinel.Tool {

    companion object {
        private const val SCHEME_PACKAGE = "package"
    }

    override fun name(): Int = R.string.sentinel_app_info

    override fun listener(): View.OnClickListener = View.OnClickListener {
        it.context.startActivity(
            Intent().apply {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.fromParts(SCHEME_PACKAGE, it.context.packageName, null)
            }
        )
    }
}