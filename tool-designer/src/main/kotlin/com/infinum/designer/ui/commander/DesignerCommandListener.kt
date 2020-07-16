package com.infinum.designer.ui.commander

import android.os.Bundle
import android.os.Message

class DesignerCommandListener(
    private val onShowGridOverlay: () -> Unit,
    private val onHideGridOverlay: () -> Unit,
    private val onUpdateGridOverlay: (Bundle) -> Unit,
    private val onShowMockupOverlay: () -> Unit,
    private val onHideMockupOverlay: () -> Unit,
    private val onUpdateMockupOverlay: (Bundle) -> Unit,
    private val onShowColorPickerOverlay: () -> Unit,
    private val onHideColorPickerOverlay: () -> Unit
) {

    fun onGridCommand(message: Message) {
        DesignerCommand(message.arg1)
            ?.let { command ->
                when (command) {
                    DesignerCommand.SHOW -> onShowGridOverlay()
                    DesignerCommand.HIDE -> onHideGridOverlay()
                    DesignerCommand.UPDATE -> onUpdateGridOverlay(message.obj as Bundle)
                }
            }
    }

    fun onMockupCommand(message: Message) {
        DesignerCommand(message.arg1)
            ?.let { command ->
                when (command) {
                    DesignerCommand.SHOW -> onShowMockupOverlay()
                    DesignerCommand.HIDE -> onHideMockupOverlay()
                    DesignerCommand.UPDATE -> onUpdateMockupOverlay(message.obj as Bundle)
                }
            }
    }

    fun onColorPickerCommand(message: Message) {
        DesignerCommand(message.arg1)
            ?.let { command ->
                when (command) {
                    DesignerCommand.SHOW -> onShowColorPickerOverlay()
                    DesignerCommand.HIDE -> onHideColorPickerOverlay()
                    DesignerCommand.UPDATE -> Unit
                }
            }
    }
}
