package com.infinum.sentinel

import android.view.View
import androidx.annotation.StringRes

@Suppress("unused")
class Sentinel private constructor(
    @Suppress("UNUSED_PARAMETER") tools: Set<Tool> = setOf()
) {

    companion object {

        @JvmStatic
        @JvmOverloads
        fun watch(tools: Set<Tool> = setOf()): Sentinel =
            lazyOf(Sentinel(tools)).value

        @JvmStatic
        fun show() = Unit
    }

    interface Tool {

        @StringRes
        fun name(): Int

        fun listener(): View.OnClickListener
    }
}
