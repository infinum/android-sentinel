package com.infinum.sentinel.ui.logger

import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.infinum.sentinel.R
import com.infinum.sentinel.SentinelFileTree
import com.infinum.sentinel.databinding.SentinelActivityLoggerBinding
import com.infinum.sentinel.ui.logger.models.FlowBuffer
import com.infinum.sentinel.ui.logger.storage.AllowedTags
import com.infinum.sentinel.ui.logs.LogsActivity
import com.infinum.sentinel.ui.shared.BounceEdgeEffectFactory
import com.infinum.sentinel.ui.shared.LogFileResolver
import com.infinum.sentinel.ui.shared.setup
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber


public class LoggerActivity : AppCompatActivity() {

    private companion object {
        private const val MIME_TYPE_TEXT = "text/plain"
    }

    private lateinit var binding: SentinelActivityLoggerBinding

    private lateinit var logFile: File

    private val buffer = Timber.forest()
        .filterIsInstance<SentinelFileTree>()
        .firstOrNull()?.buffer
        ?: FlowBuffer()

    private val adapter = LoggerAdapter(
        onListChanged = { isEmpty ->
            showEmptyState(isEmpty)
        },
        onClick = {
            ShareCompat.IntentBuilder(this)
                .setText(it.asJSONString())
                .setType(MIME_TYPE_TEXT)
                .startChooser()
        }
    )

    @Suppress("LongMethod", "CyclomaticComplexMethod")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_YES -> false
                Configuration.UI_MODE_NIGHT_NO -> true
                else -> null
            }?.let {
                WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars =
                    it
            } ?: run {
                WindowInsetsControllerCompat(
                    window,
                    window.decorView
                ).isAppearanceLightStatusBars = true
            }
        } else {
            window.statusBarColor = ContextCompat.getColor(this, android.R.color.black)
            WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars =
                true
        }

        binding = SentinelActivityLoggerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            toolbar.setNavigationOnClickListener { finish() }
            toolbar.subtitle = (
                    packageManager.getApplicationLabel(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            packageManager.getApplicationInfo(
                                packageName,
                                PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong())
                            )
                        } else {
                            @Suppress("DEPRECATION")
                            packageManager.getApplicationInfo(
                                packageName,
                                PackageManager.GET_META_DATA
                            )
                        }
                    ) as? String
                    ) ?: getString(R.string.sentinel_name)
            toolbar.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.search -> {
                        toolbar.menu.findItem(R.id.logs).isVisible = false
                        toolbar.menu.findItem(R.id.share).isVisible = false
                        true
                    }

                    R.id.logs -> {
                        showLogs()
                        true
                    }

                    R.id.share -> {
                        shareToday()
                        true
                    }

                    else -> false
                }
            }
            (toolbar.menu.findItem(R.id.search)?.actionView as? SearchView)?.setup(
                hint = getString(R.string.sentinel_search),
                onSearchClosed = {
                    toolbar.menu.findItem(R.id.logs).isVisible = true
                    toolbar.menu.findItem(R.id.share).isVisible = true
                    data()
                },
                onQueryTextChanged = { query ->
                    filter(query)
                }
            )
            recyclerView.layoutManager = LinearLayoutManager(
                recyclerView.context,
                LinearLayoutManager.VERTICAL,
                false
            )
            recyclerView.adapter = adapter
            recyclerView.edgeEffectFactory = BounceEdgeEffectFactory()
            recyclerView.addItemDecoration(
                DividerItemDecoration(
                    recyclerView.context,
                    LinearLayoutManager.VERTICAL
                )
            )
        }

        val logFileResolver = LogFileResolver(this)
        logFile = logFileResolver.createOrOpenFile()
        binding.logNameView.text = logFile.name

        data()
    }

    private fun data() {
        buffer
            .asFlow()
            .flowOn(Dispatchers.IO)
            .map { entries ->
                if (AllowedTags.value.isEmpty()) {
                    entries
                } else {
                    entries.filter { entry -> AllowedTags.value.contains(entry.tag) }
                }
            }
            .onEach { entries -> adapter.submitList(entries) }
            .launchIn(lifecycleScope)
    }

    private fun filter(query: String?) {
        lifecycleScope.launch { withContext(Dispatchers.IO) { buffer.filter(query) } }
    }

    private fun showLogs() {
        startActivity(Intent(this, LogsActivity::class.java))
    }

    private fun shareToday() {
        lifecycleScope.launch {
            val uri: Uri = withContext(Dispatchers.IO) {
                FileProvider.getUriForFile(
                    this@LoggerActivity,
                    "${this@LoggerActivity.packageName}.sentinel.logprovider",
                    logFile
                )
            }
            ShareCompat.IntentBuilder(this@LoggerActivity)
                .addStream(uri)
                .setType(MIME_TYPE_TEXT)
                .apply {
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                .startChooser()
        }
    }

    private fun showEmptyState(isEmpty: Boolean) =
        with(binding) {
            recyclerView.isGone = isEmpty
            emptyStateLayout.isVisible = isEmpty
        }
}
