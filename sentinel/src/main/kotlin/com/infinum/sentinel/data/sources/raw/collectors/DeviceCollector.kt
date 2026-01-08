package com.infinum.sentinel.data.sources.raw.collectors

import android.content.Context
import android.os.Build
import android.provider.Settings
import com.infinum.sentinel.data.models.raw.DeviceData
import com.infinum.sentinel.domain.collectors.Collectors
import com.infinum.sentinel.extensions.density
import com.infinum.sentinel.extensions.fontScale
import com.infinum.sentinel.extensions.heightPixels
import com.infinum.sentinel.extensions.screenSize
import com.infinum.sentinel.extensions.widthPixels
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import me.tatarka.inject.annotations.Inject

@Inject
internal class DeviceCollector(
    private val context: Context,
) : Collectors.Device {
    override fun invoke() =
        DeviceData(
            autoTime =
                Settings.Global.getInt(
                    context.contentResolver,
                    Settings.Global.AUTO_TIME,
                    0,
                ) == 1,
            autoTimezone =
                Settings.Global.getInt(
                    context.contentResolver,
                    Settings.Global.AUTO_TIME_ZONE,
                    0,
                ) == 1,
            isRooted = checkRootPrimary() || checkRootSecondary() || checkRootTertiary(),
            screenWidth = "${context.widthPixels} px",
            screenHeight = "${context.heightPixels} px",
            screenSize = "${context.screenSize} ″",
            screenDpi = "${context.density} dpi",
            fontScale = context.fontScale,
        )

    private fun checkRootPrimary(): Boolean {
        val buildTags = Build.TAGS
        return buildTags != null && buildTags.contains("test-keys")
    }

    private fun checkRootSecondary(): Boolean {
        val paths =
            arrayOf(
                "/system/app/Superuser.apk",
                "/sbin/su",
                "/system/bin/su",
                "/system/xbin/su",
                "/data/local/xbin/su",
                "/data/local/bin/su",
                "/system/sd/xbin/su",
                "/system/bin/failsafe/su",
                "/data/local/su",
                "/su/bin/su",
            )
        for (path in paths) {
            if (File(path).exists()) return true
        }
        return false
    }

    @Suppress("TooGenericExceptionCaught", "SwallowedException")
    private fun checkRootTertiary(): Boolean {
        var process: Process? = null
        return try {
            process = Runtime.getRuntime().exec(arrayOf("/system/xbin/which", "su"))
            val stream = BufferedReader(InputStreamReader(process.inputStream))
            stream.readLine() != null
        } catch (t: Throwable) {
            // Do nothing
            false
        } finally {
            process?.destroy()
        }
    }
}
