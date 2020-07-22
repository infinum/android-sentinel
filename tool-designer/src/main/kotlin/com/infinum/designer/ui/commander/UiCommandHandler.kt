package com.infinum.designer.ui.commander

import android.os.Handler
import android.os.Message

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