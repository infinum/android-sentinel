package com.infinum.designer.ui.commander

enum class DesignerCommandType(val code: Int) {
    CLIENT(code = 1),
    GRID(code = 2),
    MOCKUP(code = 3),
    COLOR_PICKER(code = 4);

    companion object {

        operator fun invoke(code: Int) = values().firstOrNull { it.code == code }
    }
}