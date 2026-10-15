package com.infinum.sentinel.ui.networkproxy

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.infinum.sentinel.domain.networkproxy.models.ProxyRule
import com.infinum.sentinel.domain.networkproxy.models.RecentCall
import com.infinum.sentinel.domain.networkproxy.models.RuleMode
import com.infinum.sentinel.tool.networkproxy.R
import com.infinum.sentinel.tool.networkproxy.databinding.SentinelItemNetworkProxyEmptyBinding
import com.infinum.sentinel.tool.networkproxy.databinding.SentinelItemNetworkProxyHeaderBinding
import com.infinum.sentinel.tool.networkproxy.databinding.SentinelItemNetworkProxyRecentBinding
import com.infinum.sentinel.tool.networkproxy.databinding.SentinelItemNetworkProxyRuleBinding

internal class NetworkProxyAdapter(
    private val onRuleClick: (ProxyRule) -> Unit,
    private val onRuleLongClick: (ProxyRule) -> Unit,
    private val onRuleToggled: (ProxyRule, Boolean) -> Unit,
    private val onRecentClick: (RecentCall) -> Unit,
) : ListAdapter<NetworkProxyListItem, RecyclerView.ViewHolder>(Diff) {
    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is NetworkProxyListItem.Header -> TYPE_HEADER
            is NetworkProxyListItem.Empty -> TYPE_EMPTY
            is NetworkProxyListItem.Rule -> TYPE_RULE
            is NetworkProxyListItem.Recent -> TYPE_RECENT
        }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            TYPE_HEADER -> HeaderViewHolder(SentinelItemNetworkProxyHeaderBinding.inflate(inflater, parent, false))
            TYPE_EMPTY -> EmptyViewHolder(SentinelItemNetworkProxyEmptyBinding.inflate(inflater, parent, false))
            TYPE_RULE -> RuleViewHolder(SentinelItemNetworkProxyRuleBinding.inflate(inflater, parent, false))
            else -> RecentViewHolder(SentinelItemNetworkProxyRecentBinding.inflate(inflater, parent, false))
        }
    }

    override fun onBindViewHolder(
        holder: RecyclerView.ViewHolder,
        position: Int,
    ) {
        when (val item = getItem(position)) {
            is NetworkProxyListItem.Header -> (holder as HeaderViewHolder).bind(item)
            is NetworkProxyListItem.Empty -> (holder as EmptyViewHolder).bind(item)
            is NetworkProxyListItem.Rule -> (holder as RuleViewHolder).bind(item.rule, onRuleClick, onRuleLongClick, onRuleToggled)
            is NetworkProxyListItem.Recent -> (holder as RecentViewHolder).bind(item.call, onRecentClick)
        }
    }

    private class HeaderViewHolder(private val binding: SentinelItemNetworkProxyHeaderBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: NetworkProxyListItem.Header) {
            binding.title.setText(item.title)
        }
    }

    private class EmptyViewHolder(private val binding: SentinelItemNetworkProxyEmptyBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: NetworkProxyListItem.Empty) {
            binding.message.setText(item.message)
        }
    }

    private class RuleViewHolder(private val binding: SentinelItemNetworkProxyRuleBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            rule: ProxyRule,
            onClick: (ProxyRule) -> Unit,
            onLongClick: (ProxyRule) -> Unit,
            onToggled: (ProxyRule, Boolean) -> Unit,
        ) = with(binding) {
            method.text = rule.method.name
            path.text = rule.pathPattern
            StatusCodeStyle.apply(statusCode, rule.statusCode)
            mode.setText(
                if (rule.mode == RuleMode.MOCK) {
                    R.string.sentinel_network_proxy_mode_mocked
                } else {
                    R.string.sentinel_network_proxy_mode_modified
                },
            )
            enabledSwitch.setOnCheckedChangeListener(null)
            enabledSwitch.isChecked = rule.enabled
            enabledSwitch.setOnCheckedChangeListener { _, isChecked -> onToggled(rule, isChecked) }
            root.setOnClickListener { onClick(rule) }
            root.setOnLongClickListener {
                onLongClick(rule)
                true
            }
        }
    }

    private class RecentViewHolder(private val binding: SentinelItemNetworkProxyRecentBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(
            call: RecentCall,
            onClick: (RecentCall) -> Unit,
        ) = with(binding) {
            method.text = call.method
            path.text = call.path
            StatusCodeStyle.apply(statusCode, call.statusCode)
            time.text = DateUtils.getRelativeTimeSpanString(call.timestamp, System.currentTimeMillis(), DateUtils.MINUTE_IN_MILLIS)
            mockedTag.isVisible = call.mocked
            root.setOnClickListener { onClick(call) }
        }
    }

    private object Diff : DiffUtil.ItemCallback<NetworkProxyListItem>() {
        override fun areItemsTheSame(
            oldItem: NetworkProxyListItem,
            newItem: NetworkProxyListItem,
        ): Boolean =
            when {
                oldItem is NetworkProxyListItem.Rule && newItem is NetworkProxyListItem.Rule -> oldItem.rule.id == newItem.rule.id
                oldItem is NetworkProxyListItem.Recent && newItem is NetworkProxyListItem.Recent ->
                    oldItem.call.method == newItem.call.method && oldItem.call.path == newItem.call.path
                else -> oldItem == newItem
            }

        override fun areContentsTheSame(
            oldItem: NetworkProxyListItem,
            newItem: NetworkProxyListItem,
        ): Boolean = oldItem == newItem
    }

    private companion object {
        const val TYPE_HEADER = 0
        const val TYPE_EMPTY = 1
        const val TYPE_RULE = 2
        const val TYPE_RECENT = 3
    }
}
