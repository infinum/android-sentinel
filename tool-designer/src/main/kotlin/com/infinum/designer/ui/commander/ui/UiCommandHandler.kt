package com.infinum.designer.ui.commander.ui

import android.os.Handler
import android.os.Message
import com.infinum.designer.ui.commander.DesignerCommandTarget

class UiCommandHandler(
    private val commandListener: UiCommandListener
) : Handler() {

    override fun handleMessage(message: Message) {
        DesignerCommandTarget(message.what)
            ?.let { commandType ->
                when (commandType) {
                    DesignerCommandTarget.CLIENT -> commandListener.onClientCommand(message)
                    else -> super.handleMessage(message)
                }
            } ?: super.handleMessage(message)
    }
}