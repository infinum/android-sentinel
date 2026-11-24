package com.infinum.sentinel.ui.deeplinks.editor

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.infinum.sentinel.R
import com.infinum.sentinel.domain.deeplinks.models.HostSpec
import com.infinum.sentinel.domain.deeplinks.models.PathSpec

/**
 * The helper text of a host field the tester fills in: what the declared host accepts. A fixed host explains
 * nothing, so it has none.
 */
@Composable
internal fun hostHint(host: HostSpec): String? =
    when (host) {
        is HostSpec.Wildcard -> stringResource(R.string.sentinel_deep_links_editor_hint_host_suffix, host.suffix)
        HostSpec.AnyHost -> stringResource(R.string.sentinel_deep_links_editor_hint_any_host)
        HostSpec.None -> stringResource(R.string.sentinel_deep_links_editor_hint_optional_host)
        is HostSpec.Literal -> null
    }

/** The helper text of a path field the tester fills in: what the declared path accepts. */
@Composable
internal fun pathHint(path: PathSpec): String? =
    when (path) {
        is PathSpec.Prefix -> stringResource(R.string.sentinel_deep_links_editor_hint_path_prefix, path.value)
        is PathSpec.Pattern -> stringResource(R.string.sentinel_deep_links_editor_hint_path_pattern, path.value)
        is PathSpec.AdvancedPattern -> stringResource(R.string.sentinel_deep_links_editor_hint_path_advanced_pattern, path.value)
        is PathSpec.Suffix -> stringResource(R.string.sentinel_deep_links_editor_hint_path_suffix, path.value)
        PathSpec.None -> stringResource(R.string.sentinel_deep_links_editor_hint_optional_path)
        is PathSpec.Literal -> null
    }
