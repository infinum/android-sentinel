package com.infinum.sentinel.ui.tools

import android.content.Intent
import android.util.Log
import android.view.View
import com.airbnb.android.showkase.annotation.ShowkaseRootModule
import com.airbnb.android.showkase.ui.ShowkaseBrowserActivity
import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.tool.showkase.R
import kotlin.reflect.KClass

/**
 * Specific wrapper tool around Showkase.
 *
 * Opens the Showkase browser for the app's own root module, passed in as [rootModule].
 * Tool Activity will launch with FLAG_ACTIVITY_SINGLE_TOP and FLAG_ACTIVITY_NEW_TASK flags.
 *
 * The consuming app supplies the Showkase browser runtime and applies the Showkase KSP
 * processor itself, variant-scoped, as described in this module's README.
 *
 * @param rootModule the app's own implementation of ShowkaseRootModule, annotated with `@ShowkaseRoot`
 */
public data class ShowkaseTool(
    private val rootModule: KClass<out ShowkaseRootModule>,
) : Sentinel.Tool {

    override fun icon(): Int? = null

    override fun name(): Int = R.string.sentinel_showkase

    override fun listener(): View.OnClickListener = View.OnClickListener { view ->
        try {
            view.context.startActivity(
                ShowkaseBrowserActivity.getIntent(view.context, rootModule.java.canonicalName.orEmpty()).apply {
                    flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                },
            )
        } catch (error: NoClassDefFoundError) {
            // The browser is compileOnly here, so this is what the runtime throws when the app
            // did not supply it to this variant.
            Log.e("Sentinel", "Add com.airbnb.android:showkase to the variants using tool-showkase.", error)
        }
    }
}
