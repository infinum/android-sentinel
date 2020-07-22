package com.infinum.designer.ui.commander

import android.os.Message
import android.os.Messenger
import android.os.RemoteException

abstract class AbstractCommander(
    private val outgoingMessenger: Messenger
) {

    var bound: Boolean = false

    protected fun sendMessage(message: Message) {
        if (bound) {
            try {
                outgoingMessenger.send(message)
            } catch (e: RemoteException) {
                e.printStackTrace()
            }
        }
    }
}