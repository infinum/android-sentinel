package com.infinum.designer.ui.commander

import android.os.Bundle
import android.os.Message
import android.os.Messenger

class ServiceCommandListener(
    private val onRegister: (Messenger) -> Unit,
    private val onShowGridOverlay: () -> Unit,
    private val onHideGridOverlay: () -> Unit,
    private val onUpdateGridOverlayHorizontalColor: (Bundle) -> Unit,
    private val onUpdateGridOverlayVerticalColor: (Bundle) -> Unit,
    private val onUpdateGridOverlayHorizontalGap: (Bundle) -> Unit,
    private val onUpdateGridOverlayVerticalGap: (Bundle) -> Unit,
    private val onShowMockupOverlay: () -> Unit,
    private val onHideMockupOverlay: () -> Unit,
    private val onUpdateMockupOverlayOpacity: (Bundle) -> Unit,
    private val onUpdateMockupOverlayPortraitUri: (Bundle) -> Unit,
    private val onUpdateMockupOverlayLandscapeUri: (Bundle) -> Unit,
    private val onShowColorPickerOverlay: () -> Unit,
    private val onHideColorPickerOverlay: () -> Unit,
    private val onUpdateColorPickerOverlayColorMode: (Bundle) -> Unit,
    private val onUnregister: () -> Unit
) {

    fun onClientCommand(message: Message) =
        DesignerCommand(message.arg1)
            ?.let { command ->
                when (command) {
                    DesignerCommand.REGISTER -> onRegister(message.replyTo)
                    DesignerCommand.UNREGISTER -> onUnregister()
                    else -> throw NotImplementedError()
                }
            }

    fun onGridCommand(message: Message) =
        DesignerCommand(message.arg1)
            ?.let { command ->
                when (command) {
                    DesignerCommand.SHOW -> onShowGridOverlay()
                    DesignerCommand.HIDE -> onHideGridOverlay()
                    DesignerCommand.UPDATE -> {
                        DesignerCommandParameter(message.arg2)?.let { parameter ->
                            when (parameter) {
                                DesignerCommandParameter.COLOR_HORIZONTAL ->
                                    onUpdateGridOverlayHorizontalColor(message.obj as Bundle)
                                DesignerCommandParameter.COLOR_VERTICAL ->
                                    onUpdateGridOverlayVerticalColor(message.obj as Bundle)
                                DesignerCommandParameter.GAP_HORIZONTAL ->
                                    onUpdateGridOverlayHorizontalGap(message.obj as Bundle)
                                DesignerCommandParameter.GAP_VERTICAL ->
                                    onUpdateGridOverlayVerticalGap(message.obj as Bundle)
                                else -> throw NotImplementedError()
                            }
                        }
                    }
                    else -> throw NotImplementedError()
                }
            }

    fun onMockupCommand(message: Message) =
        DesignerCommand(message.arg1)
            ?.let { command ->
                when (command) {
                    DesignerCommand.SHOW -> onShowMockupOverlay()
                    DesignerCommand.HIDE -> onHideMockupOverlay()
                    DesignerCommand.UPDATE -> {
                        DesignerCommandParameter(message.arg2)?.let { parameter ->
                            when (parameter) {
                                DesignerCommandParameter.OPACITY ->
                                    onUpdateMockupOverlayOpacity(message.obj as Bundle)
                                DesignerCommandParameter.URI_PORTRAIT ->
                                    onUpdateMockupOverlayPortraitUri(message.obj as Bundle)
                                DesignerCommandParameter.URI_LANDSCAPE ->
                                    onUpdateMockupOverlayLandscapeUri(message.obj as Bundle)
                                else -> throw NotImplementedError()
                            }
                        }
                    }
                    else -> throw NotImplementedError()
                }
            }

    fun onColorPickerCommand(message: Message) =
        DesignerCommand(message.arg1)
            ?.let { command ->
                when (command) {
                    DesignerCommand.SHOW -> onShowColorPickerOverlay()
                    DesignerCommand.HIDE -> onHideColorPickerOverlay()
                    DesignerCommand.UPDATE -> {
                        DesignerCommandParameter(message.arg2)?.let { parameter ->
                            when (parameter) {
                                DesignerCommandParameter.COLOR_MODE ->
                                    onUpdateColorPickerOverlayColorMode(message.obj as Bundle)
                                else -> throw NotImplementedError()
                            }
                        }
                    }
                    else -> throw NotImplementedError()
                }
            }
}
