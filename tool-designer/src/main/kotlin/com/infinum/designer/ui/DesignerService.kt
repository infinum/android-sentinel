package com.infinum.designer.ui

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PixelFormat
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import androidx.core.view.isVisible
import com.infinum.designer.R
import com.infinum.designer.databinding.DesignerOverlayBinding
import com.infinum.designer.extensions.dpToPx
import com.infinum.designer.ui.commander.DesignerCommand
import com.infinum.designer.ui.commander.DesignerCommandType
import com.infinum.designer.ui.models.ServiceAction


class DesignerService : Service() {

    companion object {
        private const val CHANNEL_ID = "12345"
        private const val NOTIFICATION_ID = 555
    }

    private lateinit var windowManager: WindowManager
    private lateinit var viewBinding: DesignerOverlayBinding
    private lateinit var incomingMessenger: Messenger
    private var overlayView: View? = null

    private var isRunning: Boolean = false

    private var isOverlayShown: Boolean = false
    private var isGridShown: Boolean = false
    private var isMockupShown: Boolean = false
    private var isColorPickerShown: Boolean = false

    private var horizontalGridLineColor: Int = 0
    private var verticalGridLineColor: Int = 0
    private var horizontalGridSize: Int = 1
    private var verticalGridSize: Int = 1

    private var mockupOpacity: Float = 0.0f
    private var mockupPortraitUri: Uri? = null
    private var mockupLandscapeUri: Uri? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        viewBinding = DesignerOverlayBinding.inflate(
            LayoutInflater.from(this),
            null,
            false
        )
        showNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.action?.let {
            ServiceAction(it)?.let { action ->
                when (action) {
                    ServiceAction.START -> startService()
                    ServiceAction.STOP -> stopService()
                    ServiceAction.RESET -> resetOverlay()
                }
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        incomingMessenger = Messenger(IncomingHandler())
        return incomingMessenger.binder
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (overlayView != null && overlayView?.parent != null) {
            restartOverlay()
        }
    }

    private fun startService() {
        if (isRunning) {
            return
        }
        isRunning = true
    }

    private fun stopService() {
        removeOverlay()
        try {
            stopForeground(true)
            stopSelf()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        isRunning = false
    }

    private fun showNotification() {
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.designer_ic_pick)
            .setOngoing(false)
            .setAutoCancel(true)
            .setContentTitle(getString(R.string.designer_title))
            .setContentIntent(buildSettingsIntent())
            .setDeleteIntent(buildStopIntent())
            .addAction(
                NotificationCompat.Action(
                    0,
                    "Settings",
                    buildSettingsIntent()
                )
            )
            .addAction(
                NotificationCompat.Action(
                    0,
                    "Reset",
                    buildResetIntent()
                )
            )
            .addAction(
                NotificationCompat.Action(
                    0,
                    "Stop",
                    buildStopIntent()
                )
            )
            .build()
            .also {
                startForeground(NOTIFICATION_ID, it)
            }
    }

    private fun buildSettingsIntent(): PendingIntent =
        PendingIntent.getActivity(
            this,
            0,
            Intent(this, DesignerActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT
        )

    private fun buildResetIntent(): PendingIntent =
        PendingIntent.getService(
            this,
            0,
            Intent(this, DesignerService::class.java).apply {
                action = ServiceAction.RESET.code
            },
            PendingIntent.FLAG_UPDATE_CURRENT
        )

    private fun buildStopIntent(): PendingIntent =
        PendingIntent.getService(
            this,
            0,
            Intent(this, DesignerService::class.java).apply {
                action = ServiceAction.STOP.code
            },
            PendingIntent.FLAG_CANCEL_CURRENT
        )

    private fun resetOverlay() {
        with(viewBinding.gridView) {
            isVisible = isGridShown
            updateHorizontalColor(horizontalGridLineColor)
            updateVerticalColor(verticalGridLineColor)
            updateHorizontalSize(horizontalGridSize)
            updateVerticalSize(verticalGridSize)
        }

        horizontalGridLineColor = Color.RED
        verticalGridLineColor = Color.BLUE
        horizontalGridSize = 8.0f.dpToPx(this)
        verticalGridSize = 8.0f.dpToPx(this)

        mockupOpacity = 0.2f
        mockupPortraitUri = null
        mockupLandscapeUri = null


        restartOverlay()
    }

    private fun restartOverlay() {
        removeOverlay()
        addOverlay()
    }

    private fun addOverlay() {
        overlayView = viewBinding.root

        with(viewBinding.gridView) {
            isVisible = isGridShown
            updateHorizontalColor(horizontalGridLineColor)
            updateVerticalColor(verticalGridLineColor)
            updateHorizontalSize(horizontalGridSize)
            updateVerticalSize(verticalGridSize)
        }

        with(viewBinding.mockupView) {
            isVisible = isMockupShown
            alpha = mockupOpacity
            setImageURI(
                if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    mockupPortraitUri
                } else {
                    mockupLandscapeUri
                }
            )
        }

        windowManager.addView(
            overlayView,
            WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW + 38,
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT
            ).apply {
                flags = (WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                format = PixelFormat.RGBA_8888
            }
        )
        isOverlayShown = true
    }

    private fun removeOverlay() {
        isOverlayShown = false
        try {
            if (overlayView != null && overlayView?.parent != null) {
                windowManager.removeView(overlayView)
                overlayView = null
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateGrid(message: Message) {
        DesignerCommand(message.arg1)
            ?.let { command ->
                when (command) {
                    DesignerCommand.SHOW -> showGridOverlay()
                    DesignerCommand.HIDE -> hideGridOverlay()
                    DesignerCommand.UPDATE -> updateGridOverlay(message.obj as Bundle)
                }
            }
    }

    private fun updateMockup(message: Message) {
        DesignerCommand(message.arg1)
            ?.let { command ->
                when (command) {
                    DesignerCommand.SHOW -> showMockupOverlay()
                    DesignerCommand.HIDE -> hideMockupOverlay()
                    DesignerCommand.UPDATE -> updateMockupOverlay(message.obj as Bundle)
                }
            }
    }

    private fun updateColorPicker(message: Message) {
        DesignerCommand(message.arg1)
            ?.let { command ->
                when (command) {
                    DesignerCommand.SHOW -> showColorPickerOverlay()
                    DesignerCommand.HIDE -> hideColorPickerOverlay()
                    DesignerCommand.UPDATE -> {
                    }
                }
            }
    }

    private fun showGridOverlay() {
        isGridShown = true
        restartOverlay()
    }

    private fun hideGridOverlay() {
        isGridShown = false
        restartOverlay()
    }

    private fun updateGridOverlay(bundle: Bundle) {
        horizontalGridLineColor = bundle.getInt("horizontal_grid_line_color", 0)
        verticalGridLineColor = bundle.getInt("vertical_grid_line_color", 0)
        horizontalGridSize = bundle.getInt("horizontal_grid_size", 0)
        verticalGridSize = bundle.getInt("vertical_grid_size", 0)
        restartOverlay()
    }

    private fun showMockupOverlay() {
        isMockupShown = true
        restartOverlay()
    }

    private fun hideMockupOverlay() {
        isMockupShown = false
        restartOverlay()
    }

    private fun updateMockupOverlay(bundle: Bundle) {
        mockupOpacity = bundle.getFloat("opacity", 0.0f)
        mockupPortraitUri = run {
            val uri = bundle.getString("portraitUri", "")
            if (uri.isNullOrBlank().not()) {
                Uri.parse(uri)
            } else {
                null
            }
        }
        mockupLandscapeUri = run {
            val uri = bundle.getString("landscapeUri", "")
            if (uri.isNullOrBlank().not()) {
                Uri.parse(uri)
            } else {
                null
            }
        }
        restartOverlay()
    }

    private fun showColorPickerOverlay() {
        isColorPickerShown = true
        restartOverlay()
    }

    private fun hideColorPickerOverlay() {
        isColorPickerShown = false
        restartOverlay()
    }

    inner class IncomingHandler : Handler() {

        override fun handleMessage(message: Message) {
            DesignerCommandType(message.what)
                ?.let { commandType ->
                    when (commandType) {
                        DesignerCommandType.GRID -> updateGrid(message)
                        DesignerCommandType.MOCKUP -> updateMockup(message)
                        DesignerCommandType.COLOR_PICKER -> updateColorPicker(message)
                    }
                } ?: super.handleMessage(message)
        }
    }
}