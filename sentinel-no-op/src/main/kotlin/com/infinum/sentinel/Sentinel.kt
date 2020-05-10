package com.infinum.sentinel

import android.view.View
import androidx.annotation.StringRes

@Suppress("unused")
class Sentinel private constructor(
    @Suppress("UNUSED_PARAMETER") tools: Set<Tool> = setOf()
) {

    companion object {

        fun watch(tools: Set<Tool>): Sentinel =
            lazyOf(Sentinel(tools)).value
    }

    fun show() = Unit

    interface Tool {

        @StringRes
        fun name(): Int

        fun listener(): View.OnClickListener
    }
}
