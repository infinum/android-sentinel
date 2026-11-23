package com.infinum.sentinel.ui.tools

import android.view.View
import com.infinum.sentinel.Sentinel

/**
 * Specific wrapper tool that lists the deep links declared in the app manifest and lets you
 * launch them. Example links given in [exampleLinks] are shown and launchable even if the
 * manifest does not declare them.
 */
@Suppress("UnusedPrivateMember")
public data class DeepLinkTool
@JvmOverloads
constructor(
    public val exampleLinks: List<ExampleLink> = listOf(),
    private val listener: View.OnClickListener =
        View.OnClickListener {
            // no - op
        },
) : Sentinel.Tool {
    /**
     * A link shown in the Deep links tool, even if the manifest does not declare it.
     */
    public data class ExampleLink
    @JvmOverloads
    constructor(
        public val uri: String,
        public val label: String? = null,
    )

    override fun name(): Int = 0

    override fun listener(): View.OnClickListener =
        View.OnClickListener {
            // no - op
        }
}
