package com.infinum.designer.ui.models

enum class PermissionRequest(val requestCode: Int) {
    OVERLAY(requestCode = 444);

    companion object {

        operator fun invoke(requestCode: Int) =
            values().firstOrNull { it.requestCode == requestCode }
    }
}
