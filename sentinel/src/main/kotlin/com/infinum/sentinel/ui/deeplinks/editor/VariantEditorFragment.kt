package com.infinum.sentinel.ui.deeplinks.editor

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RestrictTo
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.infinum.sentinel.R
import com.infinum.sentinel.databinding.SentinelFragmentVariantEditorBinding
import com.infinum.sentinel.domain.deeplinks.models.HostSpec
import com.infinum.sentinel.domain.deeplinks.models.PathSpec
import com.infinum.sentinel.extensions.viewModels
import com.infinum.sentinel.ui.deeplinks.dialogs.ConfirmationDialogFragment
import com.infinum.sentinel.ui.shared.base.BaseChildFragment
import com.infinum.sentinel.ui.shared.delegates.viewBinding

@RestrictTo(RestrictTo.Scope.LIBRARY)
internal class VariantEditorFragment :
    BaseChildFragment<VariantEditorState, VariantEditorEvent>(R.layout.sentinel_fragment_variant_editor) {
    companion object {
        fun newInstance(args: Bundle) = VariantEditorFragment().apply { arguments = args }

        const val TAG: String = "VariantEditorFragment"

        private const val KEY_STATE = "KEY_VARIANT_EDITOR_STATE"
        private const val KEY_FOCUS_HANDLED = "KEY_VARIANT_EDITOR_FOCUS_HANDLED"
        private const val REQUEST_DISCARD = "REQUEST_VARIANT_EDITOR_DISCARD"
    }

    override val binding: SentinelFragmentVariantEditorBinding by viewBinding(
        SentinelFragmentVariantEditorBinding::bind,
    )

    override val viewModel: VariantEditorViewModel by viewModels()

    private val backCallback =
        object : OnBackPressedCallback(false) {
            override fun handleOnBackPressed() = requestClose()
        }

    private var rowsBinder: VariantParamRowsBinder? = null
    private var focus = VariantEditorFocus.NONE
    private var isFocusHandled = false
    private var isFormBound = false
    private var isDirty = false
    private var pendingFocusRowId: Long? = null
    private var lastData: VariantEditorState.Data? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        focus =
            arguments?.getString(VariantEditorActivity.KEY_FOCUS)?.let { name ->
                VariantEditorFocus.entries.firstOrNull { it.name == name }
            } ?: VariantEditorFocus.NONE
        viewModel.setTarget(targetFromArguments())
        viewModel.setFocus(focus)
        isFocusHandled = savedInstanceState?.getBoolean(KEY_FOCUS_HANDLED) ?: false
        savedInstanceState?.getString(KEY_STATE)?.let(viewModel::restore)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupInsets()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, backCallback)
        childFragmentManager.setFragmentResultListener(REQUEST_DISCARD, viewLifecycleOwner) { _, result ->
            if (result.getBoolean(ConfirmationDialogFragment.RESULT_CONFIRMED)) requireActivity().finish()
        }
        rowsBinder =
            VariantParamRowsBinder(
                container = binding.paramsContainer,
                onKeyChanged = viewModel::onKeyChanged,
                onValueChanged = viewModel::onValueChanged,
                onRemoveClick = viewModel::removeRow,
            )
        binding.addParameterButton.setOnClickListener { viewModel.addRow() }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        viewModel.savedStateJson()?.let { outState.putString(KEY_STATE, it) }
        outState.putBoolean(KEY_FOCUS_HANDLED, isFocusHandled)
    }

    override fun onDestroyView() {
        rowsBinder?.clear()
        rowsBinder = null
        lastData = null
        isFormBound = false
        super.onDestroyView()
    }

    override fun onState(state: VariantEditorState) {
        when (state) {
            is VariantEditorState.Data -> showData(state)
            VariantEditorState.Missing -> showMissing()
            VariantEditorState.Saved -> requireActivity().finish()
        }
    }

    override fun onEvent(event: VariantEditorEvent) {
        when (event) {
            is VariantEditorEvent.FocusRow -> {
                pendingFocusRowId = event.rowId
                applyPendingRowFocus()
            }
            VariantEditorEvent.ValidationFailed -> showValidationFailed()
            VariantEditorEvent.SaveFailed ->
                Snackbar
                    .make(binding.root, R.string.sentinel_deep_links_editor_save_failed, Snackbar.LENGTH_LONG)
                    .show()
        }
    }

    private fun targetFromArguments(): VariantEditorTarget? {
        val variantId = arguments?.getString(VariantEditorActivity.KEY_VARIANT_ID)
        val baseKey = arguments?.getString(VariantEditorActivity.KEY_BASE_KEY)
        return when {
            variantId != null -> VariantEditorTarget.Existing(variantId)
            baseKey != null ->
                VariantEditorTarget.NewFromBase(baseKey, arguments?.getString(VariantEditorActivity.KEY_COMPONENT_NAME))
            else -> null
        }
    }

    private fun setupToolbar() {
        with(binding.toolbar) {
            setNavigationOnClickListener { requestClose() }
            setOnMenuItemClickListener {
                val isSave = it.itemId == R.id.save
                if (isSave) viewModel.save()
                isSave
            }
        }
    }

    private fun setupInsets() {
        val barTypes = WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout()
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { _, insets ->
            val bars = insets.getInsets(barTypes)
            binding.toolbar.updatePadding(left = bars.left, right = bars.right)
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.scrollView) { target, insets ->
            val bars = insets.getInsets(barTypes)
            val keyboard = insets.getInsets(WindowInsetsCompat.Type.ime())
            target.updatePadding(left = bars.left, right = bars.right)
            // A margin and not padding, so the scroll view shrinks above the keyboard and keeps the focused field visible.
            target.updateLayoutParams<ViewGroup.MarginLayoutParams> { bottomMargin = maxOf(bars.bottom, keyboard.bottom) }
            insets
        }
    }

    /** Closes the screen, after asking when there are unsaved changes. */
    private fun requestClose() {
        val fragmentManager = childFragmentManager
        if (!isDirty) {
            requireActivity().finish()
        } else if (!fragmentManager.isStateSaved && fragmentManager.findFragmentByTag(ConfirmationDialogFragment.TAG) == null) {
            ConfirmationDialogFragment
                .newInstance(
                    requestKey = REQUEST_DISCARD,
                    title = getString(R.string.sentinel_deep_links_editor_discard_title),
                    message = getString(R.string.sentinel_deep_links_editor_discard_message),
                    positive = getString(R.string.sentinel_deep_links_editor_discard),
                    negative = getString(R.string.sentinel_deep_links_editor_keep_editing),
                ).show(fragmentManager, ConfirmationDialogFragment.TAG)
        }
    }

    private fun showMissing() {
        isDirty = false
        backCallback.isEnabled = false
        binding.toolbar.menu.findItem(R.id.save)?.isVisible = false
        binding.scrollView.isGone = true
        binding.missingView.isVisible = true
    }

    private fun showData(state: VariantEditorState.Data) {
        lastData = state
        binding.scrollView.isVisible = true
        binding.missingView.isGone = true
        isDirty = state.isDirty
        backCallback.isEnabled = state.isDirty
        val isFirstRender = !isFormBound
        if (isFirstRender) bindFormText(state)
        showParts(state)
        if (rowsBinder?.bind(state.draft.rows) == true) applyPendingRowFocus()
        rowsBinder?.showErrors(state.validation.emptyKeyRowIds, state.isShowingErrors)
        binding.previewView.text = state.previewLink
        binding.warningView.isVisible = state.validation.isPatternMismatch
        if (isFirstRender) focusNameIfNeeded()
    }

    /** Sets the texts once and only then starts forwarding edits, so setting them doesn't echo back. */
    private fun bindFormText(state: VariantEditorState.Data) {
        isFormBound = true
        with(binding) {
            toolbar.setTitle(
                if (state.draft.variantId == null) {
                    R.string.sentinel_deep_links_editor_new_title
                } else {
                    R.string.sentinel_deep_links_editor_edit_title
                },
            )
            baseLinkView.text = state.form.baseText
            nameInput.setText(state.draft.name)
            hostInput.setText(state.draft.host)
            pathInput.setText(state.draft.path)
            nameInput.doAfterTextChanged { viewModel.onNameChanged(it?.toString().orEmpty()) }
            hostInput.doAfterTextChanged { viewModel.onHostChanged(it?.toString().orEmpty()) }
            pathInput.doAfterTextChanged { viewModel.onPathChanged(it?.toString().orEmpty()) }
        }
    }

    private fun showParts(state: VariantEditorState.Data) {
        val form = state.form
        val isShowingErrors = state.isShowingErrors
        // A fixed field explains nothing; one the tester fills in says what the declared part accepts.
        showPart(
            binding.hostInputLayout,
            form.hostMode,
            state.draft.host,
            form.spec?.host?.takeIf { form.hostMode != FieldMode.READ_ONLY }?.let(::hostHint),
            isShowingErrors && state.validation.isHostMissing,
        )
        showPart(
            binding.pathInputLayout,
            form.pathMode,
            state.draft.path,
            form.spec?.path?.takeIf { form.pathMode != FieldMode.READ_ONLY }?.let(::pathHint),
            isShowingErrors && state.validation.isPathMissing,
        )
    }

    private fun showPart(
        layout: TextInputLayout,
        mode: FieldMode,
        value: String,
        hint: String?,
        isErrorShown: Boolean,
    ) {
        layout.isVisible = mode != FieldMode.READ_ONLY || value.isNotEmpty()
        layout.isEnabled = mode != FieldMode.READ_ONLY
        layout.helperText = hint
        layout.error = if (isErrorShown) getString(R.string.sentinel_deep_links_editor_part_required) else null
    }

    private fun hostHint(host: HostSpec): String? =
        when (host) {
            is HostSpec.Wildcard -> getString(R.string.sentinel_deep_links_editor_hint_any_subdomain, host.suffix)
            HostSpec.AnyHost -> getString(R.string.sentinel_deep_links_editor_hint_any_host)
            HostSpec.None -> getString(R.string.sentinel_deep_links_editor_hint_optional_host)
            is HostSpec.Literal -> null
        }

    private fun pathHint(path: PathSpec): String? =
        when (path) {
            is PathSpec.Prefix -> getString(R.string.sentinel_deep_links_editor_hint_path_prefix, path.value)
            is PathSpec.Pattern -> getString(R.string.sentinel_deep_links_editor_hint_path_pattern, path.value)
            is PathSpec.AdvancedPattern -> getString(R.string.sentinel_deep_links_editor_hint_path_advanced_pattern, path.value)
            is PathSpec.Suffix -> getString(R.string.sentinel_deep_links_editor_hint_path_suffix, path.value)
            PathSpec.None -> getString(R.string.sentinel_deep_links_editor_hint_optional_path)
            is PathSpec.Literal -> null
        }

    /** Tells the tester why Save did nothing and moves focus to the first field that needs filling in. */
    private fun showValidationFailed() {
        Snackbar
            .make(binding.root, R.string.sentinel_deep_links_editor_fix_errors, Snackbar.LENGTH_LONG)
            .show()
        val state = lastData
        if (state != null) {
            val firstEmptyKeyRowId = state.draft.rows.firstOrNull { it.rowId in state.validation.emptyKeyRowIds }?.rowId
            val target =
                when {
                    state.validation.isHostMissing -> binding.hostInput
                    state.validation.isPathMissing -> binding.pathInput
                    else -> firstEmptyKeyRowId?.let { rowsBinder?.keyInputOf(it) }
                }
            target?.let(::focusAndShowKeyboard)
        }
    }

    private fun focusNameIfNeeded() {
        if (focus == VariantEditorFocus.NAME && !isFocusHandled) {
            isFocusHandled = true
            binding.nameInput.setSelection(binding.nameInput.length())
            focusAndShowKeyboard(binding.nameInput)
        }
    }

    /** Focuses the key field of the pending row once that row is shown. */
    private fun applyPendingRowFocus() {
        val rowId = pendingFocusRowId
        val input = rowId?.let { rowsBinder?.keyInputOf(it) }
        if (input != null) {
            pendingFocusRowId = null
            focusAndShowKeyboard(input)
        }
    }

    private fun focusAndShowKeyboard(view: View) {
        val window = requireActivity().window
        view.post {
            view.requestFocus()
            WindowCompat.getInsetsController(window, view).show(WindowInsetsCompat.Type.ime())
        }
    }
}
