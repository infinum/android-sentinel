package com.infinum.sentinel.ui.logs

import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.infinum.sentinel.tool.timber.R
import com.infinum.sentinel.tool.timber.databinding.SentinelActivityLogsBinding
import com.infinum.sentinel.ui.shared.BounceEdgeEffectFactory
import com.infinum.sentinel.ui.shared.LogFileResolver
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

public class LogsActivity : AppCompatActivity() {
    private companion object {
        private const val MIME_TYPE_TEXT = "text/plain"
    }

    @Suppress("LateinitUsage")
    private lateinit var binding: SentinelActivityLogsBinding

    private val adapter =
        LogsAdapter(
            onListChanged = { isEmpty ->
                showEmptyState(isEmpty)
            },
            onDelete = {
                deleteLog(it)
            },
            onShare = {
                shareLog(it)
            },
        )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
                window.decorView,
            ).isAppearanceLightStatusBars = true
        }

        binding = SentinelActivityLogsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            toolbar.setNavigationOnClickListener { finish() }
            toolbar.subtitle = (
                packageManager.getApplicationLabel(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        packageManager.getApplicationInfo(
                            packageName,
                            PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong()),
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        packageManager.getApplicationInfo(
                            packageName,
                            PackageManager.GET_META_DATA,
                        )
                    },
                ) as? String
            ) ?: getString(R.string.sentinel_name)

            recyclerView.layoutManager =
                LinearLayoutManager(
                    recyclerView.context,
                    LinearLayoutManager.VERTICAL,
                    false,
                )
            recyclerView.adapter = adapter
            recyclerView.edgeEffectFactory = BounceEdgeEffectFactory()
            recyclerView.addItemDecoration(
                DividerItemDecoration(
                    recyclerView.context,
                    LinearLayoutManager.VERTICAL,
                ),
            )
        }

        data()
    }

    private fun data() {
        LogFileResolver(this)
            .logsDir()
            .listFiles()
            .orEmpty()
            .asFlow()
            .flowOn(Dispatchers.IO)
            .onEach { files ->
                val allFiles = adapter.currentList + files
                adapter.submitList(allFiles.sortedByDescending { it.lastModified() })
            }.launchIn(lifecycleScope)
    }

    private fun deleteLog(logFile: File) {
        val ok = logFile.delete()
        if (ok) {
            val allFiles = adapter.currentList - logFile
            adapter.submitList(allFiles.sortedByDescending { it.lastModified() })
        }
    }

    private fun shareLog(logFile: File) {
        lifecycleScope.launch {
            val uri: Uri =
                withContext(Dispatchers.IO) {
                    FileProvider.getUriForFile(
                        this@LogsActivity,
                        "${this@LogsActivity.packageName}.sentinel.logprovider",
                        logFile,
                    )
                }
            ShareCompat
                .IntentBuilder(this@LogsActivity)
                .addStream(uri)
                .setType(MIME_TYPE_TEXT)
                .apply {
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }.startChooser()
        }
    }

    private fun showEmptyState(isEmpty: Boolean) =
        with(binding) {
            recyclerView.isGone = isEmpty
            emptyStateLayout.isVisible = isEmpty
        }
}
