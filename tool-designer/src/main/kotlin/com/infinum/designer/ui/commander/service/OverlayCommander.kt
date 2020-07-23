package com.infinum.designer.ui.commander.service

import android.os.Bundle
import android.os.Message
import android.os.Messenger
import com.infinum.designer.ui.commander.AbstractCommander
import com.infinum.designer.ui.commander.DesignerCommand
import com.infinum.designer.ui.commander.DesignerCommandTarget

abstract class OverlayCommander(
    outgoingMessenger: Messenger,
    private val replyToMessenger: Messenger
) : AbstractCommander(outgoingMessenger) {

    fun register() {
        sendMessage(
            Message.obtain(
                null,
                DesignerCommandTarget.CLIENT.code,
                DesignerCommand.REGISTER.code,
                0,
                0
            ).apply { replyTo = replyToMessenger }
        )
    }

    protected fun showGrid() {
        sendMessage(
            Message.obtain(
                null,
                DesignerCommandTarget.GRID.code,
                DesignerCommand.SHOW.code,
                0
            )
        )
    }

    protected fun hideGrid() {
        sendMessage(
            Message.obtain(
                null,
                DesignerCommandTarget.GRID.code,
                DesignerCommand.HIDE.code,
                0
            )
        )
    }

    protected fun updateGrid(parameter: OverlayCommandParameter, params: Bundle) {
        sendMessage(
            Message.obtain(
                null,
                DesignerCommandTarget.GRID.code,
                DesignerCommand.UPDATE.code,
                parameter.code,
                params
            )
        )
    }

    protected fun showMockup() {
        sendMessage(
            Message.obtain(
                null,
                DesignerCommandTarget.MOCKUP.code,
                DesignerCommand.SHOW.code,
                0
            )
        )
    }

    protected fun hideMockup() {
        sendMessage(
            Message.obtain(
                null,
                DesignerCommandTarget.MOCKUP.code,
                DesignerCommand.HIDE.code,
                0
            )
        )
    }

    protected fun updateMockup(parameter: OverlayCommandParameter, params: Bundle) {
        sendMessage(
            Message.obtain(
                null,
                DesignerCommandTarget.MOCKUP.code,
                DesignerCommand.UPDATE.code,
                parameter.code,
                params
            )
        )
    }

    protected fun showMagnifier() {
        sendMessage(
            Message.obtain(
                null,
                DesignerCommandTarget.MAGNIFIER.code,
                DesignerCommand.SHOW.code,
                0
            )
        )
    }

    protected fun hideMagnifier() {
        sendMessage(
            Message.obtain(
                null,
                DesignerCommandTarget.MAGNIFIER.code,
                DesignerCommand.HIDE.code,
                0
            )
        )
    }

    protected fun updateMagnifier(parameter: OverlayCommandParameter, params: Bundle) {
        sendMessage(
            Message.obtain(
                null,
                DesignerCommandTarget.MAGNIFIER.code,
                DesignerCommand.UPDATE.code,
                parameter.code,
                params
            )
        )
    }

    fun unregister() {
        sendMessage(
            Message.obtain(
                null,
                DesignerCommandTarget.CLIENT.code,
                DesignerCommand.UNREGISTER.code,
                0,
                0
            ).apply { replyTo = replyToMessenger }
        )
    }
}