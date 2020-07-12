package com.infinum.designer.ui.models

enum class ServiceAction(val code: String) {
    START(code = "888"),
    STOP(code = "777"),
    RESET(code = "999");

    companion object {

        operator fun invoke(code: String) =
            values().firstOrNull { it.code == code }
    }
}
