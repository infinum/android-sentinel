package com.infinum.sentinel.ui.deeplinks

import android.content.Intent
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RestrictTo
import androidx.core.net.toUri
import androidx.core.view.WindowInsetsControllerCompat
import com.infinum.sentinel.ui.deeplinks.compose.SentinelTheme
import com.infinum.sentinel.ui.deeplinks.di.DeepLinksGraph
import com.infinum.sentinel.ui.deeplinks.launch.DeepLinkLauncher

/**
 * Hosts the Deep links tool. Its screens (the list, a link's breakdown and the variant editor) are Navigation 3
 * entries in [DeepLinksNavDisplay]; the activity only provides the calls that need a `Context`.
 */
@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class DeepLinksActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The window setup of Sentinel's other screens: edge to edge, with dark status bar icons unless it's night.
        enableEdgeToEdge()
        val isNight = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = !isNight
        // Before setContent: the screens' view models take their repositories from the graph.
        DeepLinksGraph.init(applicationContext)

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
            copy = { label, value -> copyDeepLinkText(label, value) },
            share = ::shareDeepLinkText,
        )

    override fun onPause() {
        super.onPause()
        // No close animation, as on Sentinel's other screens.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            overrideActivityTransition(OVERRIDE_TRANSITION_CLOSE, 0, 0)
        } else {
            @Suppress("DEPRECATION")
            overridePendingTransition(0, 0)
        }
    }

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
