package com.infinum.sentinel.sample

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.infinum.sentinel.sample.databinding.ActivityDeepLinkBinding

/**
 * Receives the sample's declared deep links (home, catalog products and web offers)
 * and shows the link with its query parameters, so testers can check what arrived.
 */
class DeepLinkActivity : AppCompatActivity() {
    @Suppress("LateinitUsage")
    private lateinit var viewBinding: ActivityDeepLinkBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        viewBinding = ActivityDeepLinkBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        setUpDeepLinkScreen(viewBinding, R.string.deep_link_title)

        showLink(intent)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        showLink(intent)
    }

    private fun showLink(intent: Intent) {
        val uri: Uri? = intent.data
        viewBinding.link.text = uri?.toString() ?: getString(R.string.deep_link_no_link)
        // Explicit intents skip intent filters, so the link may be opaque (for example mailto:),
        // and Uri.getQueryParameter* throws for opaque URIs.
        viewBinding.parameters.text =
            uri
                ?.takeIf { it.isHierarchical }
                ?.queryParameterNames
                ?.flatMap { name ->
                    uri.getQueryParameters(name).map { value -> getString(R.string.deep_link_parameter, name, value) }
                }?.joinToString(separator = "\n")
                ?.ifEmpty { null }
                ?: getString(R.string.deep_link_no_parameters)
    }
}
