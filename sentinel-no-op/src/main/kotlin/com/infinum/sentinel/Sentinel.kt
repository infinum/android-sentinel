package com.infinum.sentinel

import android.content.Context
import android.view.View
import androidx.annotation.StringRes

@Suppress("unused")
class Sentinel(
    @Suppress("UNUSED_PARAMETER") context: Context,
    @Suppress("UNUSED_PARAMETER") tools: List<Tool>
) {

    companion object {
        private var INSTANCE: Sentinel? = null

        fun watch(context: Context, tools: List<Tool>): Sentinel {
            if (INSTANCE == null) {
                INSTANCE = Sentinel(context, tools)
            }
            return INSTANCE as Sentinel
        }
    }

    fun show() = Unit

    interface Tool {

        @StringRes
        fun name(): Int

        fun listener(): View.OnClickListener
    }
}
