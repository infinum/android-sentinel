package com.infinum.sentinel.sample

import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.airbnb.android.showkase.annotation.ShowkaseComposable

@ShowkaseComposable(name = "Primary button", group = "Buttons")
@Composable
fun PrimaryButtonSample() {
    Button(onClick = {}) {
        Text(text = "Show Sentinel")
    }
}

@ShowkaseComposable(name = "Title", group = "Text")
@Composable
fun TitleTextSample() {
    Text(text = "Sentinel sample", style = MaterialTheme.typography.h6)
}
