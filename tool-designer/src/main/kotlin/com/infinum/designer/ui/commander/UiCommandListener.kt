package com.infinum.designer.ui.commander

import android.os.Bundle
import android.os.Message
import android.os.Messenger

class UiCommandListener(
    private val onRegister: (Bundle) -> Unit,
    private val onUpdate: (Bundle) -> Unit,
    private val onUnregister: (Bundle) -> Unit
) {

    fun onClientCommand(message: Message) =
        DesignerCommand(message.arg1)
            ?.let { command ->
                when (command) {
                    DesignerCommand.REGISTER -> onRegister(message.obj as Bundle)
                    DesignerCommand.UPDATE -> onUpdate(message.obj as Bundle)
                    DesignerCommand.UNREGISTER -> onUnregister(message.obj as Bundle)
                    else -> throw NotImplementedError()
                }
            }
}
