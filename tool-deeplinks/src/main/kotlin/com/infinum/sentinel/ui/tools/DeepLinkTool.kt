package com.infinum.sentinel.ui.tools

import android.content.Intent
import android.view.View
import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.domain.deeplinks.ExampleLinksRegistry
import com.infinum.sentinel.domain.deeplinks.models.ExampleLinkData
import com.infinum.sentinel.tool.deeplinks.R
import com.infinum.sentinel.ui.deeplinks.DeepLinksActivity

/**
 * Specific wrapper tool that lists the deep links declared in the app manifest and lets you
 * launch them. Example links given in [exampleLinks] are shown and launchable even if the
 * manifest does not declare them. The tool shows the example links of the last [DeepLinkTool] the app
 * created before the Deep links screen first opened in the process.
 *
 * @property exampleLinks links to show next to the declared ones.
 * @param listener opens the Deep links screen; override it only to change what the tool button does.
 */
public data class DeepLinkTool
@JvmOverloads
constructor(
    public val exampleLinks: List<ExampleLink> = listOf(),
    private val listener: View.OnClickListener =
        View.OnClickListener {
            it.context.startActivity(
                Intent(
                    it.context,
                    DeepLinksActivity::class.java,
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                },
            )
        },
) : Sentinel.Tool {
    init {
        ExampleLinksRegistry.links = exampleLinks.map { ExampleLinkData(it.uri, it.label) }
    }

    /**
     * A link shown in the Deep links tool, even if the manifest does not declare it.
     *
     * @property uri the link to show and launch.
     * @property label an optional name shown with the link.
     */
    public data class ExampleLink
    @JvmOverloads
    constructor(
        public val uri: String,
        public val label: String? = null,
    )

    /**
     * A dedicated name for this tool
     *
     * @return a String resource that will be used to generate a name for a Button in Tools UI
     */
    override fun name(): Int = R.string.sentinel_deep_links

    /**
     * A callback to be invoked when this view is clicked.
     *
     * @return an assigned OnClickListener that will be used to generate a Button in Tools UI
     */
    override fun listener(): View.OnClickListener = listener
}
