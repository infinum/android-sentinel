package com.infinum.sentinel.ui.networkproxy

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.infinum.sentinel.domain.networkproxy.NetworkProxyPreferences
import com.infinum.sentinel.domain.networkproxy.RecentCallStore
import com.infinum.sentinel.domain.networkproxy.models.HttpMethod
import com.infinum.sentinel.domain.networkproxy.models.ProxyRule
import com.infinum.sentinel.domain.networkproxy.models.RecentCall
import com.infinum.sentinel.domain.networkproxy.models.RuleMode
import com.infinum.sentinel.tool.networkproxy.R
import com.infinum.sentinel.tool.networkproxy.databinding.SentinelFragmentNetworkProxyRuleBinding

internal class NetworkProxyRuleFragment : Fragment() {
    companion object {
        const val TAG: String = "NetworkProxyRuleFragment"

        private const val ARG_RULE_ID = "ruleId"
        private const val ARG_METHOD = "method"
        private const val ARG_PATH = "path"
        private const val ARG_STATUS_CODE = "statusCode"
        private const val ARG_PREFILL_BODY = "prefillBody"
        private const val PREVIEW_LINES = 8
        private const val PREVIEW_LINE_CHARS = 200

        fun newInstance(arguments: Bundle): NetworkProxyRuleFragment = NetworkProxyRuleFragment().apply { this.arguments = arguments }

        fun argumentsForNewRule(): Bundle = Bundle()

        fun argumentsForRule(rule: ProxyRule): Bundle = Bundle().apply { putString(ARG_RULE_ID, rule.id) }

        fun argumentsForRecentCall(call: RecentCall): Bundle =
            Bundle().apply {
                putString(ARG_METHOD, call.method)
                putString(ARG_PATH, call.path)
                putInt(ARG_STATUS_CODE, call.statusCode)
                putBoolean(ARG_PREFILL_BODY, true)
            }
    }

    private data class Draft(
        val method: String,
        val path: String,
        val mode: RuleMode,
        val statusCode: Int,
        val body: String?,
        val bodyMissingHint: Boolean,
        val enabled: Boolean,
    )

    private var viewBinding: SentinelFragmentNetworkProxyRuleBinding? = null
    private val binding get() = viewBinding!!

    private val preferences: NetworkProxyPreferences by lazy { NetworkProxyPreferences(requireContext()) }
    private val recentCalls: RecentCallStore = RecentCallStore.shared
    private val bodyViewModel: NetworkProxyBodyViewModel by activityViewModels()
    private var editingRule: ProxyRule? = null

    // Only a new form is filled from its rule or recent call. A recreated view gets its fields back from view state
    // and its body from the view model, and filling it again would reset the body the user just edited
    private var populatePending = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        populatePending = savedInstanceState == null
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        viewBinding = SentinelFragmentNetworkProxyRuleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        editingRule = arguments?.getString(ARG_RULE_ID)?.let(preferences::findRule)

        setupToolbar()
        setupInsets()
        setupInputs()
        if (populatePending) {
            populate(initialDraft())
            populatePending = false
        }
        renderBody()
        updateBodyEditorState()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }

    private fun setupToolbar() =
        with(binding.toolbar) {
            setNavigationOnClickListener { parentFragmentManager.popBackStack() }
            setOnMenuItemClickListener { item ->
                if (item.itemId == R.id.save) {
                    save()
                    true
                } else {
                    false
                }
            }
        }

    // A margin rather than padding so the scroll view's size changes with the keyboard, which is what makes it
    // scroll the focused field back into view
    private fun setupInsets() {
        binding.root.doOnBottomInset { bottomInset ->
            binding.nestedScrollView.updateLayoutParams<ViewGroup.MarginLayoutParams> { bottomMargin = bottomInset }
        }
    }

    private fun setupInputs() =
        with(binding) {
            methodInput.setSimpleItems(HttpMethod.entries.map { it.name }.toTypedArray())
            modeGroup.addOnButtonCheckedListener { _, _, _ -> updateBodyEditorState() }
            replaceBodySwitch.setOnCheckedChangeListener { _, _ -> updateBodyEditorState() }
            // In code because a multi-line TextView resets android:scrollHorizontally in its constructor
            bodyPreview.setHorizontallyScrolling(true)
            bodyPreview.setOnClickListener { openBodyEditor() }
        }

    private fun initialDraft(): Draft {
        val rule = editingRule
        val args = requireArguments()
        val prefillBody = args.getBoolean(ARG_PREFILL_BODY, false)
        val method = rule?.method?.name ?: HttpMethod.fromName(args.getString(ARG_METHOD) ?: HttpMethod.GET.name).name
        val path = rule?.pathPattern ?: args.getString(ARG_PATH) ?: "/"
        val recentBody = if (prefillBody) recentCalls.findMatching(HttpMethod.fromName(method), path)?.body else null
        return Draft(
            method = method,
            path = path,
            mode = rule?.mode ?: RuleMode.MODIFY,
            statusCode = rule?.statusCode ?: args.getInt(ARG_STATUS_CODE, ProxyRule.DEFAULT_STATUS_CODE),
            body = rule?.body ?: recentBody,
            bodyMissingHint = prefillBody && recentBody == null,
            enabled = rule?.enabled ?: true,
        )
    }

    private fun populate(draft: Draft) =
        with(binding) {
            methodInput.setText(draft.method, false)
            pathInput.setText(draft.path)
            modeGroup.check(if (draft.mode == RuleMode.MOCK) R.id.modeMock else R.id.modeModify)
            statusCodeInput.setText(draft.statusCode.toString())
            replaceBodySwitch.isChecked = draft.body != null
            bodyViewModel.body = draft.body.orEmpty()
            if (draft.bodyMissingHint) {
                bodyLayout.helperText = getString(R.string.sentinel_network_proxy_body_not_captured)
            }
            ruleEnabledSwitch.isChecked = draft.enabled
        }

    private fun renderBody() {
        binding.bodyPreview.setText(preview(bodyViewModel.body))
    }

    // The preview does not wrap, so each line returned here is one visible line and the marker stays within maxLines.
    // Lines are cut because a captured body can be 256 KB on a single line
    private fun preview(text: String): String {
        val lines = text.lineSequence().take(PREVIEW_LINES + 1).map { it.take(PREVIEW_LINE_CHARS) }.toList()
        val shown = if (lines.size > PREVIEW_LINES) lines.take(PREVIEW_LINES - 1) + "…" else lines
        return shown.joinToString("\n")
    }

    private fun updateBodyEditorState() =
        with(binding) {
            val mock = isMockMode()
            replaceBodySwitch.isVisible = !mock
            bodyLayout.isEnabled = mock || replaceBodySwitch.isChecked
        }

    private fun openBodyEditor() {
        // The error is restored with the rest of the view state when the form comes back, and by then the user may
        // have entered a body
        binding.bodyLayout.error = null
        hideKeyboard()
        parentFragmentManager
            .beginTransaction()
            .replace(
                android.R.id.content,
                NetworkProxyBodyFragment.newInstance(currentMethod(), currentPath()),
                NetworkProxyBodyFragment.TAG,
            ).addToBackStack(null)
            .commit()
    }

    private fun save() {
        val path = currentPath()
        val statusCode = binding.statusCodeInput.text.toString().toIntOrNull() ?: 0
        val mock = isMockMode()
        val replaceBody = mock || binding.replaceBodySwitch.isChecked
        if (!validate(path, statusCode, mock, replaceBody)) return

        val body = bodyViewModel.body
        val savedBody =
            when {
                !replaceBody -> null
                mock && body.isBlank() -> null
                else -> body
            }
        val rule =
            (editingRule ?: ProxyRule()).copy(
                enabled = binding.ruleEnabledSwitch.isChecked,
                method = HttpMethod.fromName(currentMethod()),
                pathPattern = path,
                mode = if (mock) RuleMode.MOCK else RuleMode.MODIFY,
                statusCode = statusCode,
                body = savedBody,
            )
        preferences.saveRule(rule)
        parentFragmentManager.popBackStack()
    }

    private fun validate(
        path: String,
        statusCode: Int,
        mock: Boolean,
        replaceBody: Boolean,
    ): Boolean {
        val pathError =
            when {
                !path.startsWith("/") -> R.string.sentinel_network_proxy_error_path_slash
                path.any { it.isWhitespace() } -> R.string.sentinel_network_proxy_error_path_whitespace
                else -> null
            }
        val statusError =
            if (statusCode in ProxyRule.MIN_STATUS_CODE..ProxyRule.MAX_STATUS_CODE) null else R.string.sentinel_network_proxy_error_status
        val bodyError = if (!mock && replaceBody && bodyViewModel.body.isBlank()) R.string.sentinel_network_proxy_error_body else null

        binding.pathLayout.error = pathError?.let(::getString)
        binding.statusCodeLayout.error = statusError?.let(::getString)
        binding.bodyLayout.error = bodyError?.let(::getString)
        return pathError == null && statusError == null && bodyError == null
    }

    private fun isMockMode(): Boolean = binding.modeGroup.checkedButtonId == R.id.modeMock

    private fun currentMethod(): String = binding.methodInput.text.toString()

    private fun currentPath(): String = binding.pathInput.text.toString().trim()
}
