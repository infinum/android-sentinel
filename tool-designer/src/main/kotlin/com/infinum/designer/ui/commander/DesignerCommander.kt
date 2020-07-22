package com.infinum.designer.ui.commander

import android.os.Bundle
import android.os.Message
import android.os.Messenger
import android.os.RemoteException

class DesignerCommander(
    private val outgoingMessenger: Messenger,
    private val replyToMessenger: Messenger
) {

    var bound: Boolean = false

    fun register() {
        sendMessage(
            Message.obtain(
                null,
                DesignerCommandType.CLIENT.code,
                DesignerCommand.REGISTER.code,
                0,
                0
            ).apply { replyTo = replyToMessenger }
        )
    }

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
        updateGrid(DesignerCommandParameter.COLOR_HORIZONTAL, params)

    fun updateGridVerticalColor(params: Bundle) =
        updateGrid(DesignerCommandParameter.COLOR_VERTICAL, params)

    fun updateGridHorizontalGap(params: Bundle) =
        updateGrid(DesignerCommandParameter.GAP_HORIZONTAL, params)

    fun updateGridVerticalGap(params: Bundle) =
        updateGrid(DesignerCommandParameter.GAP_VERTICAL, params)

    fun updateMockupOpacity(params: Bundle) =
        updateMockup(DesignerCommandParameter.OPACITY, params)

    fun updateMockupPortraitUri(params: Bundle) =
        updateMockup(DesignerCommandParameter.URI_PORTRAIT, params)

    fun updateMockupLandscapeUri(params: Bundle) =
        updateMockup(DesignerCommandParameter.URI_LANDSCAPE, params)

    fun updateColorPickerColorMode(params: Bundle) =
        updateColorPicker(DesignerCommandParameter.COLOR_MODE, params)

    fun unregister() {
        sendMessage(
            Message.obtain(
                null,
                DesignerCommandType.CLIENT.code,
                DesignerCommand.UNREGISTER.code,
                0,
                0
            ).apply { replyTo = replyToMessenger }
        )
    }

    private fun showGrid() {
        sendMessage(
            Message.obtain(
                null,
                DesignerCommandType.GRID.code,
                DesignerCommand.SHOW.code,
                0
            )
        )
    }

    private fun hideGrid() {
        sendMessage(
            Message.obtain(
                null,
                DesignerCommandType.GRID.code,
                DesignerCommand.HIDE.code,
                0
            )
        )
    }

    private fun updateGrid(parameter: DesignerCommandParameter, params: Bundle) {
        sendMessage(
            Message.obtain(
                null,
                DesignerCommandType.GRID.code,
                DesignerCommand.UPDATE.code,
                parameter.code,
                params
            )
        )
    }

    private fun showMockup() {
        sendMessage(
            Message.obtain(
                null,
                DesignerCommandType.MOCKUP.code,
                DesignerCommand.SHOW.code,
                0
            )
        )
    }

    private fun hideMockup() {
        sendMessage(
            Message.obtain(
                null,
                DesignerCommandType.MOCKUP.code,
                DesignerCommand.HIDE.code,
                0
            )
        )
    }

    private fun updateMockup(parameter: DesignerCommandParameter, params: Bundle) {
        sendMessage(
            Message.obtain(
                null,
                DesignerCommandType.MOCKUP.code,
                DesignerCommand.UPDATE.code,
                parameter.code,
                params
            )
        )
    }

    private fun showColorPicker() {
        sendMessage(
            Message.obtain(
                null,
                DesignerCommandType.COLOR_PICKER.code,
                DesignerCommand.SHOW.code,
                0
            )
        )
    }

    private fun hideColorPicker() {
        sendMessage(
            Message.obtain(
                null,
                DesignerCommandType.COLOR_PICKER.code,
                DesignerCommand.HIDE.code,
                0
            )
        )
    }

    private fun updateColorPicker(parameter: DesignerCommandParameter, params: Bundle) {
        sendMessage(
            Message.obtain(
                null,
                DesignerCommandType.COLOR_PICKER.code,
                DesignerCommand.UPDATE.code,
                parameter.code,
                params
            )
        )
    }

    private fun sendMessage(message: Message) {
        if (bound) {
            try {
                outgoingMessenger.send(message)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }
}