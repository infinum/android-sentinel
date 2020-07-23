package com.infinum.designer.ui.commander.ui

import android.os.Bundle
import android.os.Message
import android.os.Messenger
import com.infinum.designer.ui.commander.AbstractCommander
import com.infinum.designer.ui.commander.DesignerCommand
import com.infinum.designer.ui.commander.DesignerCommandTarget

class UiCommander(
    clientMessenger: Messenger
) : AbstractCommander(clientMessenger) {

    fun notifyRegister(params: Bundle) {
        sendMessage(
            Message.obtain(
                null,
                DesignerCommandTarget.CLIENT.code,
                DesignerCommand.REGISTER.code,
                0,
                params
            )
        )
    }

    fun notifyUnregister(params: Bundle) {
        sendMessage(
            Message.obtain(
                null,
                DesignerCommandTarget.CLIENT.code,
                DesignerCommand.UNREGISTER.code,
                0,
                params
            )
        )
    }
}