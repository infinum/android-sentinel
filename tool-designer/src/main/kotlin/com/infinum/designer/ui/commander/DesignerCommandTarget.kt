package com.infinum.designer.ui.commander

enum class DesignerCommandTarget(val code: Int) {
    CLIENT(code = 1),
    GRID(code = 2),
    MOCKUP(code = 3),
    MAGNIFIER(code = 4);

    companion object {

        operator fun invoke(code: Int) = values().firstOrNull { it.code == code }
    }
}