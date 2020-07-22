package com.infinum.designer.ui

import android.app.Service
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import androidx.core.os.bundleOf
import com.infinum.designer.builders.DesignerNotificationBuilder
import com.infinum.designer.extensions.dpToPx
import com.infinum.designer.ui.commander.DesignerCommand
import com.infinum.designer.ui.commander.DesignerCommandType
import com.infinum.designer.ui.commander.service.ServiceCommandHandler
import com.infinum.designer.ui.commander.service.ServiceCommandListener
import com.infinum.designer.ui.commander.ui.UiCommander
import com.infinum.designer.ui.models.ColorModel
import com.infinum.designer.ui.models.ServiceAction
import com.infinum.designer.ui.models.configuration.DesignerConfiguration
import com.infinum.designer.ui.overlays.grid.GridOverlay
import com.infinum.designer.ui.overlays.magnifier.MagnifierOverlay
import com.infinum.designer.ui.overlays.mockup.MockupOverlay

class DesignerService : Service() {

    private var commander: UiCommander? = null

    private var configuration = DesignerConfiguration()

    private lateinit var gridOverlay: GridOverlay
    private lateinit var mockupOverlay: MockupOverlay
    private lateinit var magnifierOverlay: MagnifierOverlay

    private var isRunning: Boolean = false

    override fun onCreate() {
        super.onCreate()

        configuration = configuration.copy(
            grid = configuration.grid.copy(
                horizontalLineColor = Color.RED,
                verticalLineColor = Color.BLUE,
                horizontalGridSize = 8.0f.dpToPx(this),
                verticalGridSize = 8.0f.dpToPx(this)
            )
        )

        gridOverlay = GridOverlay(this)
        mockupOverlay =
            MockupOverlay(this)
        magnifierOverlay =
            MagnifierOverlay(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let {
            ServiceAction(it)?.let { action ->
                when (action) {
                    ServiceAction.START -> startService()
                    ServiceAction.STOP -> stopService()
                    ServiceAction.RESET -> resetOverlays()
                }
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? =
        Messenger(
            ServiceCommandHandler(
                ServiceCommandListener(
                    onRegister = this::register,
                    onShowGridOverlay = {
                        configuration = configuration.copy(
                            grid = configuration.grid.copy(enabled = true)
                        )
                        gridOverlay.show()
                    },
                    onHideGridOverlay = {
                        configuration = configuration.copy(
                            grid = configuration.grid.copy(enabled = false)
                        )
                        gridOverlay.hide()
                    },
                    onUpdateGridOverlayHorizontalColor = {
                        configuration = configuration.copy(
                            grid = configuration.grid.copy(
                                horizontalLineColor = it.getInt(
                                    "horizontalLineColor",
                                    configuration.grid.horizontalLineColor
                                )
                            )
                        )
                        gridOverlay.update(configuration.grid)
                    },
                    onUpdateGridOverlayVerticalColor = {
                        configuration = configuration.copy(
                            grid = configuration.grid.copy(
                                verticalLineColor = it.getInt(
                                    "verticalLineColor",
                                    configuration.grid.verticalLineColor
                                )
                            )
                        )
                        gridOverlay.update(configuration.grid)
                    },
                    onUpdateGridOverlayHorizontalGap = {
                        configuration = configuration.copy(
                            grid = configuration.grid.copy(
                                horizontalGridSize = it.getInt(
                                    "horizontalGridSize",
                                    configuration.grid.horizontalGridSize
                                )
                            )
                        )
                        gridOverlay.update(configuration.grid)
                    },
                    onUpdateGridOverlayVerticalGap = {
                        configuration = configuration.copy(
                            grid = configuration.grid.copy(
                                verticalGridSize = it.getInt(
                                    "verticalGridSize",
                                    configuration.grid.horizontalGridSize
                                )
                            )
                        )
                        gridOverlay.update(configuration.grid)
                    },
                    onShowMockupOverlay = {
                        configuration = configuration.copy(
                            mockup = configuration.mockup.copy(enabled = true)
                        )
                        mockupOverlay.show()
                    },
                    onHideMockupOverlay = {
                        configuration = configuration.copy(
                            mockup = configuration.mockup.copy(enabled = false)
                        )
                        mockupOverlay.hide()
                    },
                    onUpdateMockupOverlayOpacity = {
                        configuration = configuration.copy(
                            mockup = configuration.mockup.copy(
                                opacity = it.getFloat(
                                    "opacity",
                                    configuration.mockup.opacity
                                )
                            )
                        )
                        mockupOverlay.update(configuration.mockup)
                    },
                    onUpdateMockupOverlayPortraitUri = {
                        configuration = configuration.copy(
                            mockup = configuration.mockup.copy(
                                portraitUri = it.getString(
                                    "portraitUri",
                                    configuration.mockup.portraitUri
                                )
                            )
                        )
                        mockupOverlay.update(configuration.mockup)
                    },
                    onUpdateMockupOverlayLandscapeUri = {
                        configuration = configuration.copy(
                            mockup = configuration.mockup.copy(
                                landscapeUri = it.getString(
                                    "landscapeUri",
                                    configuration.mockup.landscapeUri
                                )
                            )
                        )
                        mockupOverlay.update(configuration.mockup)
                    },
                    onShowColorPickerOverlay = {
                        configuration = configuration.copy(
                            magnifier = configuration.magnifier.copy(enabled = true)
                        )
                        magnifierOverlay.show()
                    },
                    onHideColorPickerOverlay = {
                        configuration = configuration.copy(
                            magnifier = configuration.magnifier.copy(enabled = false)
                        )
                        magnifierOverlay.hide()
                    },
                    onUpdateColorPickerOverlayColorMode = {
                        configuration = configuration.copy(
                            magnifier = configuration.magnifier.copy(
                                colorModel = ColorModel(
                                    it.getString(
                                        "colorModel",
                                        configuration.magnifier.colorModel.type
                                    )
                                ) ?: ColorModel.HEX
                            )
                        )
                        magnifierOverlay.update(configuration.magnifier)
                    },
                    onUnregister = this::unregister
                )
            )
        ).binder

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        if (gridOverlay.isShowing()) {
            gridOverlay.hide()
            gridOverlay.show()
        }
        if (magnifierOverlay.isShowing()) {
            magnifierOverlay.hide()
            magnifierOverlay.show()
        }
    }

    private fun startService() {
        if (isRunning) {
            return
        }

        DesignerNotificationBuilder(this).also { it.show() }

        configuration = configuration.copy(enabled = true)

        isRunning = true
    }

    private fun stopService() {
        gridOverlay.hide()
        mockupOverlay.hide()
        magnifierOverlay.hide()

        try {
            stopForeground(true)
            stopSelf()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        isRunning = false
    }

    private fun resetOverlays() {
        configuration = configuration.copy(
            grid = configuration.grid.copy(
                horizontalLineColor = Color.RED,
                verticalLineColor = Color.BLUE,
                horizontalGridSize = 8.0f.dpToPx(this),
                verticalGridSize = 8.0f.dpToPx(this)
            ),
            mockup = configuration.mockup.copy(
                opacity = 0.2f,
                portraitUri = null,
                landscapeUri = null
            ),
            magnifier = configuration.magnifier.copy(
                colorModel = ColorModel.HEX
            )
        )
        if (gridOverlay.isShowing()) {
            gridOverlay.reset(configuration.grid)
        }
        if (mockupOverlay.isShowing()) {
            mockupOverlay.reset(configuration.mockup)
        }
        if (magnifierOverlay.isShowing()) {
            magnifierOverlay.reset(configuration.magnifier)
        }
    }

    private fun register(client: Messenger) {
        this.commander = UiCommander(client)
        commander?.bound = true
        commander?.notifyRegister(
            bundleOf("configuration" to configuration)
        )
    }

    private fun unregister() {
        configuration = configuration.copy(
            enabled = false,
            grid = configuration.grid.copy(enabled = false),
            mockup = configuration.mockup.copy(enabled = false),
            magnifier = configuration.magnifier.copy(enabled = false)
        )

        commander?.notifyUnregister(
            bundleOf("configuration" to configuration)
        )
        commander?.bound = false
        commander = null
    }
}