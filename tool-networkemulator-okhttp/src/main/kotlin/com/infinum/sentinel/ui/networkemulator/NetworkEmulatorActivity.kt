package com.infinum.sentinel.ui.networkemulator

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Activity that hosts the network emulator configuration UI.
 * Provides controls for configuring network delay, failure rate, and variance.
 */
public class NetworkEmulatorActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set up status bar appearance
        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> false
            Configuration.UI_MODE_NIGHT_NO -> true
            else -> null
        }?.let {
            WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = it
        }

        supportFragmentManager
            .beginTransaction()
            .replace(
                android.R.id.content,
                NetworkEmulatorFragment.newInstance(),
                NetworkEmulatorFragment.TAG,
            ).commit()
    }
}
