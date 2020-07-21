package com.infinum.designer.ui.commander

import android.os.Bundle
import android.os.Message
import android.os.Messenger
import android.os.RemoteException

class DesignerCommander(
    private val outgoingMessenger: Messenger
) {

    var bound: Boolean = false

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

    fun updateGrid(params: Bundle) {
        sendMessage(
            Message.obtain(
                null,
                DesignerCommandType.GRID.code,
                DesignerCommand.UPDATE.code,
                0,
                params
            )
        )
    }

    fun updateMockup(params: Bundle) {
        sendMessage(
            Message.obtain(
                null,
                DesignerCommandType.MOCKUP.code,
                DesignerCommand.UPDATE.code,
                0,
                params
            )
        )
    }

    fun updateColorPicker(params: Bundle) {
        sendMessage(
            Message.obtain(
                null,
                DesignerCommandType.COLOR_PICKER.code,
                DesignerCommand.UPDATE.code,
                0,
                params
            )
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