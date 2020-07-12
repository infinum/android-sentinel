package com.infinum.designer.ui.commander

enum class DesignerCommand(val code: Int) {
    SHOW(code = 1),
    HIDE(code = 2),
    UPDATE(code = 3);

    companion object {

        operator fun invoke(code: Int) = values().firstOrNull { it.code == code }
    }
}