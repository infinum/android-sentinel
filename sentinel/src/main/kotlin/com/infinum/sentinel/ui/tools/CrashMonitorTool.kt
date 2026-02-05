package com.infinum.sentinel.ui.tools

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.view.View
import androidx.annotation.StringRes
import com.infinum.sentinel.R
import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.ui.crash.CrashesActivity
import com.infinum.sentinel.ui.shared.Constants

/**
 * Specific wrapper tool that monitors crashes for the application which implemented Sentinel.
 */
internal data class CrashMonitorTool(
    @StringRes private val name: Int = R.string.sentinel_crash_monitor,
    private val listener: View.OnClickListener =
        View.OnClickListener {
            it.context.startActivity(
                Intent(
                    it.context,
                    CrashesActivity::class.java,
                ).apply {
                    putExtra(
                        Constants.Keys.APPLICATION_NAME,
                        (
                            it.context.packageManager.getApplicationLabel(
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    it.context.packageManager.getApplicationInfo(
                                        it.context.packageName,
                                        PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong()),
                                    )
                                } else {
                                    @Suppress("DEPRECATION")
                                    it.context.packageManager.getApplicationInfo(
                                        it.context.packageName,
                                        PackageManager.GET_META_DATA,
                                    )
                                },
                            ) as? String
                            ) ?: it.context.getString(R.string.sentinel_name),
                    )
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                },
            )
        },
) : Sentinel.Tool {
    /**
     * A dedicated name for this tool
     *
     * @return a String resource that will be used to generate a name for a Button in Tools UI
     */
    override fun name(): Int = name

    /**
     * A callback to be invoked when this view is clicked.
     *
     * @return an assigned OnClickListener that will be used to generate a Button in Tools UI
     */
    override fun listener(): View.OnClickListener = listener
}
