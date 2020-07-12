package com.infinum.designer.ui.commander

enum class DesignerCommandType(val code: Int) {
    GRID(code = 1),
    MOCKUP(code = 2),
    COLOR_PICKER(code = 3);

    companion object {

        operator fun invoke(code: Int) = values().firstOrNull { it.code == code }
    }
}