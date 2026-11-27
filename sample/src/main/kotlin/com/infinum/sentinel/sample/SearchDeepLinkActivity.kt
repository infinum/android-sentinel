package com.infinum.sentinel.sample

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.infinum.sentinel.sample.databinding.ActivityDeepLinkBinding

/**
 * Receives `sentinelsample://search` links and shows the `q` query and every repeated `tag` parameter.
 */
class SearchDeepLinkActivity : AppCompatActivity() {
    @Suppress("LateinitUsage")
    private lateinit var viewBinding: ActivityDeepLinkBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        viewBinding = ActivityDeepLinkBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        setUpDeepLinkScreen(viewBinding, R.string.deep_link_search_title)

        showSearch(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        showSearch(intent)
    }

    private fun showSearch(intent: Intent) {
        val uri = intent.data
        // Explicit intents skip intent filters, so the link may be opaque (for example mailto:),
        // and Uri.getQueryParameter* throws for opaque URIs.
        val hierarchicalUri = uri?.takeIf { it.isHierarchical }
        val query = hierarchicalUri?.getQueryParameter("q")
        val tags = hierarchicalUri?.getQueryParameters("tag").orEmpty()

        val noValue = getString(R.string.deep_link_no_value)

        viewBinding.link.text = uri?.toString() ?: getString(R.string.deep_link_no_link)
        viewBinding.parameters.text =
            listOf(
                getString(R.string.deep_link_parameter, "q", query ?: noValue),
                getString(R.string.deep_link_parameter, "tag", tags.ifEmpty { listOf(noValue) }.joinToString()),
            ).joinToString(separator = "\n")
    }
}
