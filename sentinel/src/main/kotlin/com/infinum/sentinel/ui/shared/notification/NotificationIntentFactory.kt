package com.infinum.sentinel.ui.shared.notification

import android.content.Context
import android.content.Intent
import com.infinum.sentinel.ui.Presentation
import com.infinum.sentinel.ui.certificates.CertificatesActivity
import com.infinum.sentinel.ui.crash.CrashesActivity
import com.infinum.sentinel.ui.crash.details.CrashDetailsActivity

internal class NotificationIntentFactory(
    private val context: Context
) : IntentFactory {

    override fun crash(applicationName: String, id: Long): Array<Intent> =
        arrayOf(
            Intent(context, CrashesActivity::class.java)
                .apply {
                    putExtra(Presentation.Constants.Keys.APPLICATION_NAME, applicationName)
                },
            Intent(context, CrashDetailsActivity::class.java)
                .apply {
                    putExtra(Presentation.Constants.Keys.CRASH_ID, id)
                }
        )

    override fun certificate(applicationName: String): Array<Intent> =
        arrayOf(
            Intent(context, CertificatesActivity::class.java)
                .apply {
                    putExtra(Presentation.Constants.Keys.APPLICATION_NAME, applicationName)
                }
        )
}
