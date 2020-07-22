package com.infinum.designer.ui.commander

import android.os.Handler
import android.os.Message

class ServiceCommandHandler(
    private val commandListener: ServiceCommandListener
) : Handler() {

    override fun handleMessage(message: Message) {
        DesignerCommandType(message.what)
            ?.let { commandType ->
                when (commandType) {
                    DesignerCommandType.CLIENT -> commandListener.onClientCommand(message)
                    DesignerCommandType.GRID -> commandListener.onGridCommand(message)
                    DesignerCommandType.MOCKUP -> commandListener.onMockupCommand(message)
                    DesignerCommandType.COLOR_PICKER -> commandListener.onColorPickerCommand(message)
                }
            } ?: super.handleMessage(message)
    }
}