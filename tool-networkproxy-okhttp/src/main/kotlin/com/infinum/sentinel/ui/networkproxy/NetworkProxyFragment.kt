package com.infinum.sentinel.ui.networkproxy

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.marginBottom
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.infinum.sentinel.domain.networkproxy.NetworkProxyPreferences
import com.infinum.sentinel.domain.networkproxy.RecentCallStore
import com.infinum.sentinel.domain.networkproxy.models.ProxyRule
import com.infinum.sentinel.tool.networkproxy.R
import com.infinum.sentinel.tool.networkproxy.databinding.SentinelFragmentNetworkProxyBinding

internal class NetworkProxyFragment : Fragment() {
    companion object {
        const val TAG: String = "NetworkProxyFragment"
    }

    private var viewBinding: SentinelFragmentNetworkProxyBinding? = null
    private val binding get() = viewBinding!!

    private val preferences: NetworkProxyPreferences by lazy { NetworkProxyPreferences(requireContext()) }
    private val recentCalls: RecentCallStore = RecentCallStore.shared
    private val mainHandler = Handler(Looper.getMainLooper())

    // The interceptor notifies from OkHttp threads, and a notification can land after onStop
    private val recentCallsListener =
        RecentCallStore.Listener {
            mainHandler.post { if (viewBinding != null) refresh() }
        }

    private val adapter =
        NetworkProxyAdapter(
            onRuleClick = { rule -> openEditor(NetworkProxyRuleFragment.argumentsForRule(rule)) },
            onRuleLongClick = ::confirmDelete,
            onRuleToggled = { rule, enabled ->
                preferences.setRuleEnabled(rule.id, enabled)
                refresh()
            },
            onRecentClick = { call -> openEditor(NetworkProxyRuleFragment.argumentsForRecentCall(call)) },
        )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        viewBinding = SentinelFragmentNetworkProxyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupInsets()
        binding.recyclerView.adapter = adapter
        binding.addRuleButton.setOnClickListener { openEditor(NetworkProxyRuleFragment.argumentsForNewRule()) }
    }

    override fun onStart() {
        super.onStart()
        recentCalls.addListener(recentCallsListener)
        refresh()
    }

    override fun onStop() {
        recentCalls.removeListener(recentCallsListener)
        mainHandler.removeCallbacksAndMessages(null)
        super.onStop()
    }

    override fun onDestroyView() {
        binding.recyclerView.adapter = null
        super.onDestroyView()
        viewBinding = null
    }

    private fun setupToolbar() =
        with(binding) {
            toolbar.setNavigationOnClickListener { activity?.finish() }
            toolbar.setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.clearRecentCalls) {
                    recentCalls.clear()
                    refresh()
                    true
                } else {
                    false
                }
            }
            enabledSwitch.isChecked = preferences.isEnabled
            enabledSwitch.setOnCheckedChangeListener { _, isChecked -> preferences.isEnabled = isChecked }
        }

    private fun setupInsets() =
        with(binding) {
            val listPadding = recyclerView.paddingBottom
            val buttonMargin = addRuleButton.marginBottom
            root.doOnBottomInset { bottomInset ->
                recyclerView.updatePadding(bottom = listPadding + bottomInset)
                addRuleButton.updateLayoutParams<ViewGroup.MarginLayoutParams> { bottomMargin = buttonMargin + bottomInset }
            }
        }

    private fun refresh() {
        val rules = preferences.rules
        val recent = recentCalls.snapshot()
        val items =
            buildList {
                add(NetworkProxyListItem.Header(R.string.sentinel_network_proxy_rules))
                if (rules.isEmpty()) {
                    add(NetworkProxyListItem.Empty(R.string.sentinel_network_proxy_rules_empty))
                } else {
                    rules.forEach { add(NetworkProxyListItem.Rule(it)) }
                }
                add(NetworkProxyListItem.Header(R.string.sentinel_network_proxy_recent))
                if (recent.isEmpty()) {
                    add(NetworkProxyListItem.Empty(R.string.sentinel_network_proxy_recent_empty))
                } else {
                    recent.forEach { add(NetworkProxyListItem.Recent(it)) }
                }
            }
        adapter.submitList(items)
    }

    private fun confirmDelete(rule: ProxyRule) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.sentinel_network_proxy_delete_title)
            .setMessage(getString(R.string.sentinel_network_proxy_delete_message, rule.method.name, rule.pathPattern))
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.sentinel_network_proxy_delete) { _, _ ->
                preferences.deleteRule(rule.id)
                refresh()
            }.show()
    }

    private fun openEditor(arguments: Bundle) {
        parentFragmentManager
            .beginTransaction()
            .replace(android.R.id.content, NetworkProxyRuleFragment.newInstance(arguments), NetworkProxyRuleFragment.TAG)
            .addToBackStack(null)
            .commit()
    }
}
