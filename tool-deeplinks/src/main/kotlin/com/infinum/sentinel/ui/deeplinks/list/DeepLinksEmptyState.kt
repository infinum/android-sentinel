package com.infinum.sentinel.ui.deeplinks.list

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.infinum.sentinel.tool.deeplinks.R
import com.infinum.sentinel.ui.deeplinks.compose.SentinelTheme
import com.infinum.sentinel.R as SentinelR

/**
 * What the Deep links screen shows when the app has no links: an illustration and a message, centered.
 */
@Composable
internal fun DeepLinksEmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(painter = painterResource(SentinelR.drawable.sentinel_ic_certificates_empty), contentDescription = null)
        Text(
            text = stringResource(R.string.sentinel_deep_links_empty),
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp).padding(all = 16.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Preview(name = "Light")
@Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun DeepLinksEmptyStatePreview() {
    SentinelTheme {
        DeepLinksEmptyState()
    }
}
