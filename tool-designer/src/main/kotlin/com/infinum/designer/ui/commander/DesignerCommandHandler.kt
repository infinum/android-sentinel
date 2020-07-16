package com.infinum.designer.ui.commander

import android.os.Handler
import android.os.Message

class DesignerCommandHandler(
    private val commandListener: DesignerCommandListener
) : Handler() {

    override fun handleMessage(message: Message) {
        DesignerCommandType(message.what)
            ?.let { commandType ->
                when (commandType) {
                    DesignerCommandType.GRID -> commandListener.onGridCommand(message)
                    DesignerCommandType.MOCKUP -> commandListener.onMockupCommand(message)
                    DesignerCommandType.COLOR_PICKER -> commandListener.onColorPickerCommand(message)
                }
            } ?: super.handleMessage(message)
    }
}