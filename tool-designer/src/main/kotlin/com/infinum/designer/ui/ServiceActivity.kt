package com.infinum.designer.ui

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.os.Messenger
import androidx.annotation.Px
import androidx.annotation.RestrictTo
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentActivity
import com.infinum.designer.ui.commander.service.ServiceCommander
import com.infinum.designer.ui.commander.ui.UiCommandHandler
import com.infinum.designer.ui.commander.ui.UiCommandListener
import com.infinum.designer.ui.models.ColorModel
import com.infinum.designer.ui.models.ServiceAction
import com.infinum.designer.ui.models.configuration.DesignerConfiguration

@RestrictTo(RestrictTo.Scope.LIBRARY)
abstract class ServiceActivity : FragmentActivity() {

    private var commander: ServiceCommander? = null

    private var bound: Boolean = false

    private val serviceConnection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            commander =
                ServiceCommander(
                    Messenger(service),
                    Messenger(
                        UiCommandHandler(
                            UiCommandListener(
                                onRegister = this@ServiceActivity::onRegister,
                                onUnregister = this@ServiceActivity::onUnregister
                            )
                        )
                    )
                )
            bound = true
            commander?.bound = bound

            register()
        }

        override fun onServiceDisconnected(className: ComponentName) {
            bound = false
            commander?.bound = bound
            commander = null
        }
    }

    abstract fun setupUi(configuration: DesignerConfiguration)

    override fun onStart() {
        super.onStart()
        bindService()
    }

    override fun onStop() {
        super.onStop()
        unbindService()
    }

    protected fun createService() {
        startService()
        bindService()
    }

    protected fun destroyService() {
        unregister()
    }

    protected fun toggleGrid(shouldShow: Boolean) {
        commander?.toggleGrid(shouldShow)
    }

    protected fun updateGridHorizontalColor(color: Int) {
        commander?.updateGridHorizontalColor(
            bundleOf("horizontalLineColor" to color)
        )
    }

    protected fun updateGridVerticalColor(color: Int) {
        commander?.updateGridVerticalColor(
            bundleOf("verticalLineColor" to color)
        )
    }

    protected fun updateGridHorizontalGap(@Px gap: Int) {
        commander?.updateGridHorizontalGap(
            bundleOf("horizontalGridSize" to gap)
        )
    }

    protected fun updateGridVerticalGap(@Px gap: Int) {
        commander?.updateGridVerticalGap(
            bundleOf("verticalGridSize" to gap)
        )
    }

    protected fun toggleMockup(shouldShow: Boolean) {
        commander?.toggleMockup(shouldShow)
    }

    protected fun updateMockupOpacity(opacity: Float)  {
        commander?.updateMockupOpacity(
            bundleOf("opacity" to opacity)
        )
    }

    protected fun updateMockupPortraitUri(uri: String?) {
        commander?.updateMockupPortraitUri(
            bundleOf("portraitUri" to uri)
        )
    }

    protected fun updateMockupLandscapeUri(uri: String?) {
        commander?.updateMockupLandscapeUri(
            bundleOf("landscapeUri" to uri)
        )
    }

    protected fun toggleColorPicker(shouldShow: Boolean) {
        commander?.toggleColorPicker(shouldShow)
    }

    protected fun updateColorPickerColorModel(colorModel: ColorModel) {
        commander?.updateColorPickerColorModel(
            bundleOf(
                "colorModel" to colorModel.type
            )
        )
    }

    private fun register() {
        commander?.register()
    }

    private fun unregister() {
        commander?.unregister()
    }

    private fun startService() {
        ContextCompat.startForegroundService(
            this,
            Intent(this, DesignerService::class.java).apply {
                action = ServiceAction.START.code
            }
        )
    }

    private fun bindService() {
        if (bound.not()) {
            bindService(
                Intent(this, DesignerService::class.java),
                serviceConnection,
                0
            )
        } else {
            commander?.register()
        }
    }

    private fun stopService() {
        ContextCompat.startForegroundService(
            this,
            Intent(this, DesignerService::class.java).apply {
                action = ServiceAction.STOP.code
            }
        )
    }

    private fun unbindService() {
        if (bound) {
            unbindService(serviceConnection)
            bound = false
        }
    }

    private fun onRegister(bundle: Bundle) {
        setupUi(bundle.getParcelable("configuration") ?: DesignerConfiguration())
    }

    private fun onUnregister(bundle: Bundle) {
        setupUi(bundle.getParcelable("configuration") ?: DesignerConfiguration())
        unbindService()
        stopService()
    }
}