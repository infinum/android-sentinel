package com.infinum.designer.ui.commander.service

import android.os.Handler
import android.os.Message
import com.infinum.designer.ui.commander.DesignerCommandTarget

class ServiceCommandHandler(
    private val commandListener: ServiceCommandListener
) : Handler() {

    override fun handleMessage(message: Message) {
        DesignerCommandTarget(message.what)
            ?.let { commandType ->
                when (commandType) {
                    DesignerCommandTarget.CLIENT -> commandListener.onClientCommand(message)
                    DesignerCommandTarget.GRID -> commandListener.onGridCommand(message)
                    DesignerCommandTarget.MOCKUP -> commandListener.onMockupCommand(message)
                    DesignerCommandTarget.MAGNIFIER -> commandListener.onMagnifierCommand(message)
                }
            } ?: super.handleMessage(message)
    }
}