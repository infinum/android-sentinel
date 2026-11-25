package com.infinum.sentinel.ui.deeplinks

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.core.app.ShareCompat
import com.infinum.sentinel.tool.deeplinks.R
import com.infinum.sentinel.R as SentinelR

/**
 * Copies [value] to the clipboard under [label] and confirms it with a toast. False when there's no clipboard.
 */
internal fun Context.copyDeepLinkText(
    label: String,
    value: String,
): Boolean {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText(label, value))
    val message = if (clipboard != null) R.string.sentinel_deep_links_text_copied else R.string.sentinel_deep_links_copy_failed
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    return clipboard != null
}

/**
 * Opens the system share sheet for [text], titled with Sentinel's name.
 */
internal fun Context.shareDeepLinkText(text: String) {
    ShareCompat
        .IntentBuilder(this)
        .setChooserTitle(SentinelR.string.sentinel_name)
        .setType("text/plain")
        .setText(text)
        .startChooser()
}
