package com.infinum.sentinel.extensions

import androidx.core.app.ShareCompat
import com.infinum.sentinel.R
import com.infinum.sentinel.ui.shared.Constants

internal fun ShareCompat.IntentBuilder.shareText(text: String) =
    this.setChooserTitle(R.string.sentinel_name)
        .setType(Constants.SHARE_MIME_TYPE)
        .setText(text)
        .startChooser()
