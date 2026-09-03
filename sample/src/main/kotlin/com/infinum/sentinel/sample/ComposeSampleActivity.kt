package com.infinum.sentinel.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

/**
 * Exercises Sentinel's Compose annotation-target resolution ([com.infinum.sentinel.ui.annotations.overlay.ComposeSupport])
 * against a real composition, rather than only compiled-but-unexercised reflection code.
 *
 * Builds its own [ComposeView] (instead of the `setContent {}` activity extension) purely so it
 * can carry [R.id.compose_root]: that lets a device check confirm the C5 fallback-fix claim that
 * the app's own id on its `ComposeView` survives into the exported locator even though the
 * `AndroidComposeView` Compose hosts inside it never carries an id of its own.
 */
class ComposeSampleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val composeView =
            ComposeView(this).apply {
                id = R.id.compose_root
                setContent {
                    MaterialTheme {
                        CheckoutScreen()
                    }
                }
            }
        setContentView(composeView)
    }
}

@Composable
private fun CheckoutScreen() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Checkout")
        PrimaryButton(label = "Submit")
        PrimaryButton(label = "Cancel")
    }
}

@Composable
private fun PrimaryButton(label: String) {
    Button(
        onClick = { },
        modifier = Modifier.testTag("primary_button"),
    ) {
        Text(text = label)
    }
}
