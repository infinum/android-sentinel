package com.infinum.sentinel.ui.logger

import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ShareCompat
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.infinum.sentinel.R
import com.infinum.sentinel.SentinelTree
import com.infinum.sentinel.databinding.SentinelActivityLoggerBinding
import com.infinum.sentinel.ui.logger.models.FlowBuffer
import com.infinum.sentinel.ui.shared.BounceEdgeEffectFactory
import com.infinum.sentinel.ui.shared.setup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

public class LoggerActivity : AppCompatActivity() {

    private companion object {
        private const val MIME_TYPE_TEXT = "text/plain"
    }

    private lateinit var binding: SentinelActivityLoggerBinding

    private val buffer = Timber.forest().filterIsInstance<SentinelTree>().firstOrNull()?.buffer ?: FlowBuffer()

    private val adapter = LoggerAdapter(
        onListChanged = { isEmpty ->
            showEmptyState(isEmpty)
        },
        onClick = {
            ShareCompat.IntentBuilder(this)
                .setText(it.asString())
                .setType(MIME_TYPE_TEXT)
                .startChooser()
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_YES -> false
                Configuration.UI_MODE_NIGHT_NO -> true
                else -> null
            }?.let {
                WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = it
            } ?: run { WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true }
        } else {
            window.statusBarColor = ContextCompat.getColor(this, android.R.color.black)
            WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true
        }

        binding = SentinelActivityLoggerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with(binding) {
            toolbar.setNavigationOnClickListener { finish() }
            toolbar.subtitle = (
                packageManager.getApplicationLabel(
                    packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA)
                ) as? String
                ) ?: getString(R.string.sentinel_name)
            toolbar.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.search -> {
                        toolbar.menu.findItem(R.id.clear).isVisible = false
                        toolbar.menu.findItem(R.id.share).isVisible = false
                        true
                    }
                    R.id.clear -> {
                        clearLogger()
                        true
                    }
                    R.id.share -> {
                        shareAll()
                        true
                    }
                    else -> false
                }
            }
            (toolbar.menu.findItem(R.id.search)?.actionView as? SearchView)?.setup(
                hint = getString(R.string.sentinel_search),
                onSearchClosed = {
                    toolbar.menu.findItem(R.id.clear).isVisible = true
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
            recyclerView.addItemDecoration(DividerItemDecoration(recyclerView.context, LinearLayoutManager.VERTICAL))
        }

        data()
    }

    private fun data() {
        buffer
            .asFlow()
            .flowOn(Dispatchers.IO)
            .onEach { entries -> adapter.submitList(entries) }
            .launchIn(lifecycleScope)
    }

    private fun filter(query: String?) {
        lifecycleScope.launch { withContext(Dispatchers.IO) { buffer.filter(query) } }
    }

    private fun clearLogger() {
        lifecycleScope.launch { withContext(Dispatchers.IO) { buffer.clear() } }
    }

    private fun shareAll() {
        lifecycleScope.launch {
            val text = withContext(Dispatchers.IO) {
                buffer.asString()
            }
            ShareCompat.IntentBuilder(this@LoggerActivity)
                .setText(text)
                .setType(MIME_TYPE_TEXT)
                .startChooser()
        }
    }

    private fun showEmptyState(isEmpty: Boolean) =
        with(binding) {
            recyclerView.isGone = isEmpty
            emptyStateLayout.isVisible = isEmpty
        }
}