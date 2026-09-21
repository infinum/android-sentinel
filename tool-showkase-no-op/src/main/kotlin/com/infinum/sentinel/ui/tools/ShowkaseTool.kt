package com.infinum.sentinel.ui.tools

import android.view.View
import com.airbnb.android.showkase.annotation.ShowkaseRootModule
import com.infinum.sentinel.Sentinel
import kotlin.reflect.KClass

/**
 * No-op implementation of the Showkase wrapper tool.
 *
 * Maintains API compatibility with the debug implementation so that registering
 * `ShowkaseTool(MyRootModule::class)` from shared sources compiles in every variant.
 *
 * @param rootModule the app's own implementation of ShowkaseRootModule, ignored here
 */
public data class ShowkaseTool(
    @Suppress("unused") private val rootModule: KClass<out ShowkaseRootModule>,
) : Sentinel.Tool {

    override fun icon(): Int? = null

    override fun name(): Int = 0

    override fun listener(): View.OnClickListener = View.OnClickListener {
        // No-op
    }
}
