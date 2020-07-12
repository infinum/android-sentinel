package com.infinum.designer.ui.models

enum class MockupOrientation(val requestCode: Int) {
    PORTRAIT(requestCode = 222),
    LANDSCAPE(requestCode = 333);

    companion object {

        operator fun invoke(requestCode: Int) =
            values().firstOrNull { it.requestCode == requestCode }
    }
}
