package com.infinum.designer.ui

import android.app.Service
import android.content.Intent
import android.content.res.Configuration
import android.os.IBinder
import android.os.Messenger
import com.infinum.designer.builders.DesignerNotificationBuilder
import com.infinum.designer.ui.commander.DesignerCommandHandler
import com.infinum.designer.ui.commander.DesignerCommandListener
import com.infinum.designer.ui.models.ServiceAction
import com.infinum.designer.ui.overlays.grid.GridOverlay
import com.infinum.designer.ui.overlays.magnifier.MagnifierOverlay
import com.infinum.designer.ui.overlays.mockup.MockupOverlay

class DesignerService : Service() {

    private lateinit var gridOverlay: GridOverlay
    private lateinit var mockupOverlay: MockupOverlay
    private lateinit var magnifierOverlay: MagnifierOverlay

    private var isRunning: Boolean = false

    override fun onCreate() {
        super.onCreate()

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
            DesignerCommandHandler(
                DesignerCommandListener(
                    onShowGridOverlay = gridOverlay::show,
                    onHideGridOverlay = gridOverlay::hide,
                    onUpdateGridOverlay = gridOverlay::update,
                    onShowMockupOverlay = mockupOverlay::show,
                    onHideMockupOverlay = mockupOverlay::hide,
                    onUpdateMockupOverlay = mockupOverlay::update,
                    onShowColorPickerOverlay = magnifierOverlay::show,
                    onHideColorPickerOverlay = magnifierOverlay::hide,
                    onUpdateColorPickerOverlay = magnifierOverlay::update
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
        if (gridOverlay.isShowing()) {
            gridOverlay.reset()
        }
        if (mockupOverlay.isShowing()) {
            mockupOverlay.reset()
        }
    }
}