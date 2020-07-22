package com.infinum.designer.ui.commander.ui

import android.os.Handler
import android.os.Message
import com.infinum.designer.ui.commander.DesignerCommandType

class UiCommandHandler(
    private val commandListener: UiCommandListener
) : Handler() {

    override fun handleMessage(message: Message) {
        DesignerCommandType(message.what)
            ?.let { commandType ->
                when (commandType) {
                    DesignerCommandType.CLIENT -> commandListener.onClientCommand(message)
                    else -> super.handleMessage(message)
                }
            } ?: super.handleMessage(message)
    }
}