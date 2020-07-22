package com.infinum.designer.ui.commander.ui

import android.os.Bundle
import android.os.Message
import com.infinum.designer.ui.commander.DesignerCommand

class UiCommandListener(
    private val onRegister: (Bundle) -> Unit,
    private val onUnregister: (Bundle) -> Unit
) {

    fun onClientCommand(message: Message) =
        DesignerCommand(message.arg1)
            ?.let { command ->
                when (command) {
                    DesignerCommand.REGISTER -> onRegister(message.obj as Bundle)
                    DesignerCommand.UNREGISTER -> onUnregister(message.obj as Bundle)
                    else -> throw NotImplementedError()
                }
            }
}
