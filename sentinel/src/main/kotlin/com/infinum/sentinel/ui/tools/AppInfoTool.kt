package com.infinum.sentinel.ui.tools

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.view.View
import com.infinum.sentinel.R
import com.infinum.sentinel.Sentinel

/**
 * Specific wrapper tool around Android OS Settings page for the application which implemented Sentinel.
 */
internal class AppInfoTool : Sentinel.Tool {

    companion object {
        private const val SCHEME_PACKAGE = "package"
    }

    /**
     * A dedicated name for this tool
     *
     * @return a String resource that will be used to generate a name for a Button in Tools UI
     */
    override fun name(): Int = R.string.sentinel_app_info

    /**
     * A callback to be invoked when this view is clicked.
     *
     * @return an assigned OnClickListener that will be used to generate a Button in Tools UI
     */
    override fun listener(): View.OnClickListener = View.OnClickListener {
        it.context.startActivity(
            Intent().apply {
                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                data = Uri.fromParts(SCHEME_PACKAGE, it.context.packageName, null)
            }
        )
    }
}
