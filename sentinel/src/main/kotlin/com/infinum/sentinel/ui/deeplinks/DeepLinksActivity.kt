package com.infinum.sentinel.ui.deeplinks

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.annotation.RestrictTo
import androidx.core.app.ShareCompat
import androidx.core.net.toUri
import com.infinum.sentinel.extensions.copyToClipboard
import com.infinum.sentinel.extensions.shareText
import com.infinum.sentinel.ui.deeplinks.launch.DeepLinkLauncher
import com.infinum.sentinel.ui.shared.base.BaseChildActivity
import com.infinum.sentinel.ui.shared.compose.SentinelTheme

/**
 * Hosts the Deep links tool. Its screens (the list, a link's breakdown and the variant editor) are Navigation 3
 * entries in [DeepLinksNavDisplay]; the activity only provides the calls that need a `Context`.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class DeepLinksActivity : BaseChildActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val host = hostActions()
        setContent {
            SentinelTheme {
                DeepLinksNavDisplay(host = host)
            }
        }
    }

    private fun hostActions(): DeepLinksHostActions =
        DeepLinksHostActions(
            close = ::finish,
            launch = { link, mode -> DeepLinkLauncher(this).launch(link, mode) },
            openLinkSettings = ::openLinkSettings,
            copy = { label, value -> copyToClipboard(key = label, value = value) },
            share = { text -> ShareCompat.IntentBuilder(this).shareText(text) },
        )

    /**
     * Opens the system "Open by default" settings of this app, or its app details when that fails.
     * Below Android 12 there is nothing to open, which counts as done. False when neither screen could be opened.
     */
    private fun openLinkSettings(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true
        return startSettings(Intent(Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS, "package:$packageName".toUri())) ||
            startSettings(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", packageName, null)))
    }

    private fun startSettings(intent: Intent): Boolean =
        try {
            startActivity(intent)
            true
        } catch (ignored: RuntimeException) {
            // ActivityNotFoundException or a SecurityException from a restricted settings screen: try the next one.
            false
        }
}
