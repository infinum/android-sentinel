package com.infinum.sentinel

import android.content.Context
import android.view.View
import androidx.annotation.StringRes

@Suppress("unused")
class Sentinel private constructor(
    @Suppress("UNUSED_PARAMETER") context: Context,
    @Suppress("UNUSED_PARAMETER") tools: Set<Tool>
) {

    companion object {

        fun watch(context: Context, tools: Set<Tool>): Sentinel =
            lazyOf(Sentinel(context, tools)).value
    }

    fun show() = Unit

    interface Tool {

        @StringRes
        fun name(): Int

        fun listener(): View.OnClickListener
    }
}
