package com.infinum.sentinel.ui.networkproxy

import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.infinum.sentinel.domain.networkproxy.JsonFormatter
import com.infinum.sentinel.domain.networkproxy.RecentCallStore
import com.infinum.sentinel.domain.networkproxy.models.HttpMethod
import com.infinum.sentinel.tool.networkproxy.R
import com.infinum.sentinel.tool.networkproxy.databinding.SentinelFragmentNetworkProxyBodyBinding

internal class NetworkProxyBodyFragment : Fragment() {
    companion object {
        const val TAG: String = "NetworkProxyBodyFragment"

        private const val ARG_METHOD = "method"
        private const val ARG_PATH = "path"

        fun newInstance(
            method: String,
            path: String,
        ): NetworkProxyBodyFragment =
            NetworkProxyBodyFragment().apply {
                arguments =
                    Bundle().apply {
                        putString(ARG_METHOD, method)
                        putString(ARG_PATH, path)
                    }
            }
    }

    private var viewBinding: SentinelFragmentNetworkProxyBodyBinding? = null
    private val binding get() = viewBinding!!

    private val bodyViewModel: NetworkProxyBodyViewModel by activityViewModels()
    private val recentCalls: RecentCallStore = RecentCallStore.shared

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        viewBinding = SentinelFragmentNetworkProxyBodyBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupInsets()
        // The text lives in the view model rather than in the view's saved state
        binding.bodyInput.setText(bodyViewModel.body)
        binding.bodyInput.doAfterTextChanged { bodyViewModel.body = it?.toString().orEmpty() }
        if (savedInstanceState == null && bodyViewModel.body.isEmpty()) showKeyboard()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewBinding = null
    }

    private fun setupToolbar() =
        with(binding.toolbar) {
            setNavigationOnClickListener {
                hideKeyboard()
                parentFragmentManager.popBackStack()
            }
            menu.findItem(R.id.loadResponse).isEnabled = lastRealResponse() != null
            setOnMenuItemClickListener { item -> onMenuItemClick(item.itemId) }
        }

    private fun onMenuItemClick(itemId: Int): Boolean {
        when (itemId) {
            R.id.paste -> pasteFromClipboard()
            R.id.format -> formatBody()
            R.id.loadResponse -> lastRealResponse()?.let(binding.bodyInput::setText)
            R.id.clear -> binding.bodyInput.text?.clear()
            else -> return false
        }
        return true
    }

    private fun setupInsets() {
        binding.root.doOnBottomInset { bottomInset ->
            with(binding.bodyInput) {
                updateLayoutParams<ViewGroup.MarginLayoutParams> { bottomMargin = bottomInset }
                post { bringPointIntoView(selectionEnd) }
            }
        }
    }

    private fun showKeyboard() =
        with(binding.bodyInput) {
            requestFocus()
            WindowCompat.getInsetsController(requireActivity().window, this).show(WindowInsetsCompat.Type.ime())
        }

    private fun pasteFromClipboard() {
        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val text = clipboard.primaryClip?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.coerceToText(requireContext())
        if (text.isNullOrEmpty()) {
            Toast.makeText(requireContext(), R.string.sentinel_network_proxy_clipboard_empty, Toast.LENGTH_SHORT).show()
        } else {
            binding.bodyInput.setText(text)
        }
    }

    private fun formatBody() {
        val formatted = JsonFormatter.format(binding.bodyInput.text.toString())
        if (formatted == null) {
            Toast.makeText(requireContext(), R.string.sentinel_network_proxy_not_json, Toast.LENGTH_SHORT).show()
        } else {
            binding.bodyInput.setText(formatted)
        }
    }

    private fun lastRealResponse(): String? {
        val args = requireArguments()
        val method = HttpMethod.fromName(args.getString(ARG_METHOD))
        return recentCalls.findMatching(method, args.getString(ARG_PATH).orEmpty())?.body
    }
}
