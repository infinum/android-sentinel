package com.infinum.sentinel.ui.tools

import android.content.Intent
import android.view.View
import com.infinum.sentinel.R
import com.infinum.sentinel.Sentinel
import com.infinum.sentinel.ui.logger.LoggerActivity
import com.infinum.sentinel.ui.logger.storage.AllowedTags

/**
 * Specific wrapper tool around Timber.
 *
 * Tool Activity will launch with no additional flags.
 */

public data class TimberTool @JvmOverloads constructor(
    private val allowedTags: List<String> = emptyList(),
    private val listener: View.OnClickListener = View.OnClickListener {
        it.context.startActivity(
            Intent(
                it.context,
                LoggerActivity::class.java
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            }
        )
    }
) : Sentinel.Tool {

    /**
     * An optional set of filterable tags
     *
     * Sets a list of allowed tags for filtering stored in internal cache(AllowedTags)
     */
    init {
        AllowedTags.set(allowedTags)
    }

    /**
     * An optional icon for this tool
     *
     * @return a Drawable resource that will be used to generate an icon in a Button in Tools UI
     */
    override fun icon(): Int? = null

    /**
     * A dedicated name for this tool
     *
     * @return a String resource that will be used to generate a name for a Button in Tools UI
     */
    override fun name(): Int = R.string.sentinel_logger

    /**
     * A callback to be invoked when this view is clicked.
     *
     * @return an assigned OnClickListener that will be used to generate a Button in Tools UI
     */
    override fun listener(): View.OnClickListener = listener
}
