package com.infinum.designer.ui.commander.service

enum class OverlayCommandParameter(val code: Int) {
    COLOR_HORIZONTAL(code = 1),
    COLOR_VERTICAL(code = 2),
    GAP_HORIZONTAL(code = 3),
    GAP_VERTICAL(code = 4),
    OPACITY(code = 5),
    URI_PORTRAIT(code = 6),
    URI_LANDSCAPE(code = 7),
    COLOR_MODEL(code = 8);

    companion object {

        operator fun invoke(code: Int) = values().firstOrNull { it.code == code }
    }
}