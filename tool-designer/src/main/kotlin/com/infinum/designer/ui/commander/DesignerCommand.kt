package com.infinum.designer.ui.commander

enum class DesignerCommand(val code: Int) {
    REGISTER(code = 1),
    SHOW(code = 2),
    HIDE(code = 3),
    UPDATE(code = 4),
    UNREGISTER(code = 5);

    companion object {

        operator fun invoke(code: Int) = values().firstOrNull { it.code == code }
    }
}