package com.infinum.designer.ui

import android.app.Activity
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.Point
import android.hardware.display.DisplayManager
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
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
import kotlin.concurrent.thread

class DesignerService : Service() {

    companion object {
        private const val CHANNEL_ID = "12345"
        private const val NOTIFICATION_ID = 555
    }

    private lateinit var windowManager: WindowManager
    private lateinit var mProjectionManager: MediaProjectionManager
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

    private var mediaProjectionHandler = Handler()
    private var mediaProjection: MediaProjection? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        mProjectionManager =
            getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

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

        with(viewBinding.loupeView) {
            isVisible = isColorPickerShown
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
                    DesignerCommand.UPDATE -> Unit
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

//        thread {
//            Looper.prepare()
            mediaProjectionHandler = Handler()
//            Looper.loop()
//        }.start()

        mediaProjection = DesignerProjectionHelper.data?.let {
            mProjectionManager.getMediaProjection(
                Activity.RESULT_OK,
                it
            )
        }

        createVirtualDisplay()

        restartOverlay()
    }

    private fun hideColorPickerOverlay() {
        isColorPickerShown = false

        mediaProjectionHandler.post {
            mediaProjection?.stop()
        }

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

    private fun createVirtualDisplay() {
        val size = displaySize()
        /* The call to create a virtual display takes width and height of the virtual display and a surface
         object which will be used to save the device's screen. To the capture
         the screen as an image or a series of images we use surface of an ImageReader object. Create an
         ImageReader object as follows: */
        val mImageReader = ImageReader.newInstance(size.x, size.y, PixelFormat.RGBA_8888, 2)

/*        Once we have the MediaProjection and ImageReader objects, we can start
          capturing screen by creating a virtual display using following code:   */
        val mVirtualDisplay = mediaProjection?.createVirtualDisplay(
            "SCREENCAP_NAME",
            size.x,
            size.y,
            resources.displayMetrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY or DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
//            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            mImageReader.surface,
            null /*call backs*/,
            mediaProjectionHandler /*Handler*/
        )
        mImageReader.setOnImageAvailableListener(
            ImageAvailableListener(
                size.x,
                size.y,
                this::onNewBitmap
            ),
            mediaProjectionHandler
        )

        mediaProjection?.registerCallback(
            MediaProjectionStopCallback(
                mediaProjectionHandler,
                mVirtualDisplay,
                mImageReader,
                mediaProjection
            ),
            mediaProjectionHandler
        )
    }

    private fun onNewBitmap(bitmap: Bitmap) {
        viewBinding.loupeView.setImageBitmap(bitmap)
    }

    private fun displaySize(): Point {
        val size = Point()
        windowManager.defaultDisplay.getSize(size)
        return size
    }
}