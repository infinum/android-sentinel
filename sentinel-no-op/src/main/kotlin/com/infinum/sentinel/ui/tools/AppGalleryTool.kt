package com.infinum.sentinel.ui.tools

import android.view.View
import com.infinum.sentinel.Sentinel

/**
 * Specific wrapper tool around Huawei AppGallery in no - op.
 *
 */
@Suppress("UnusedPrivateMember")
public data class AppGalleryTool
    @JvmOverloads
    constructor(
        private val appId: String = "",
        private val listener: View.OnClickListener =
            View.OnClickListener {
                // no - op
            },
    ) : Sentinel.Tool {
        override fun name(): Int = 0

        override fun listener(): View.OnClickListener =
            View.OnClickListener {
                // no - op
            }
    }
