package com.infinum.designer.ui.commander.service

import android.os.Bundle
import android.os.Messenger

class ServiceCommander(
    outgoingMessenger: Messenger,
    replyToMessenger: Messenger
) : OverlayCommander(outgoingMessenger, replyToMessenger) {

    fun toggleGrid(shouldShow: Boolean) {
        if (shouldShow) {
            showGrid()
        } else {
            hideGrid()
        }
    }

    fun toggleMockup(shouldShow: Boolean) {
        if (shouldShow) {
            showMockup()
        } else {
            hideMockup()
        }
    }

    fun toggleColorPicker(shouldShow: Boolean) {
        if (shouldShow) {
            showColorPicker()
        } else {
            hideColorPicker()
        }
    }

    fun updateGridHorizontalColor(params: Bundle) =
        updateGrid(OverlayCommandParameter.COLOR_HORIZONTAL, params)

    fun updateGridVerticalColor(params: Bundle) =
        updateGrid(OverlayCommandParameter.COLOR_VERTICAL, params)

    fun updateGridHorizontalGap(params: Bundle) =
        updateGrid(OverlayCommandParameter.GAP_HORIZONTAL, params)

    fun updateGridVerticalGap(params: Bundle) =
        updateGrid(OverlayCommandParameter.GAP_VERTICAL, params)

    fun updateMockupOpacity(params: Bundle) =
        updateMockup(OverlayCommandParameter.OPACITY, params)

    fun updateMockupPortraitUri(params: Bundle) =
        updateMockup(OverlayCommandParameter.URI_PORTRAIT, params)

    fun updateMockupLandscapeUri(params: Bundle) =
        updateMockup(OverlayCommandParameter.URI_LANDSCAPE, params)

    fun updateColorPickerColorMode(params: Bundle) =
        updateColorPicker(OverlayCommandParameter.COLOR_MODE, params)
}