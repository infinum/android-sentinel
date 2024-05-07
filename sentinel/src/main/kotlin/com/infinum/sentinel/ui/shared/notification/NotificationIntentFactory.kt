package com.infinum.sentinel.ui.shared.notification

import android.content.Context
import android.content.Intent
import android.os.Build
import com.infinum.sentinel.ui.certificates.CertificatesActivity
import com.infinum.sentinel.ui.crash.CrashesActivity
import com.infinum.sentinel.ui.crash.details.CrashDetailsActivity
import com.infinum.sentinel.ui.settings.SettingsActivity
import com.infinum.sentinel.ui.shared.Constants
import me.tatarka.inject.annotations.Inject

@Inject
internal class NotificationIntentFactory(
    private val context: Context
) : IntentFactory {

    override fun crash(applicationName: String, id: Long): Array<Intent> =
        arrayOf(
            Intent(context, CrashesActivity::class.java)
                .apply {
                    putExtra(Constants.Keys.APPLICATION_NAME, applicationName)
                },
            Intent(context, CrashDetailsActivity::class.java)
                .apply {
                    putExtra(Constants.Keys.CRASH_ID, id)
                }
        )

    override fun certificate(applicationName: String): Array<Intent> =
        arrayOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent(context, CertificatesActivity::class.java)
                    .apply {
                        putExtra(Constants.Keys.APPLICATION_NAME, applicationName)
                    }
            } else {
                Intent(context, SettingsActivity::class.java)
            }
        )
}
