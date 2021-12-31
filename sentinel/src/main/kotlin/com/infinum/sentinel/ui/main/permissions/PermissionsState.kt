package com.infinum.sentinel.ui.main.permissions

internal sealed class PermissionsState {

    data class Data(
        val value: Map<String, Boolean>
    ) : PermissionsState()
}
