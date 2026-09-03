package com.infinum.sentinel.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

/**
 * Exercises Sentinel's Compose annotation-target resolution ([com.infinum.sentinel.ui.annotations.overlay.ComposeSupport])
 * against a real composition, rather than only compiled-but-unexercised reflection code.
 */
class ComposeSampleActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                CheckoutScreen()
            }
        }
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
