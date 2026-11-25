package com.infinum.sentinel.ui.deeplinks.launch

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.util.Log
import androidx.core.net.toUri
import com.infinum.sentinel.domain.deeplinks.models.LaunchMode

/**
 * Launches a link with a VIEW intent from [activity], the way the system or this app would.
 *
 * It never asks the package manager who can handle the link, because package visibility would make that answer
 * wrong. It starts the activity and reads the outcome from the exception instead.
 */
internal class DeepLinkLauncher(
    private val activity: Activity,
) {
    /**
     * Starts [link] in the given [mode] and returns what happened. Never throws.
     *
     * [LaunchMode.REAL_TAP] adds the BROWSABLE category and no package, like a tap in another app.
     * [LaunchMode.OPEN_IN_APP] limits the intent to this app's package and adds no category.
     */
    fun launch(
        link: String,
        mode: LaunchMode,
    ): LaunchResult =
        try {
            activity.startActivity(intent(link, mode))
            LaunchResult.Launched
        } catch (e: ActivityNotFoundException) {
            Log.v(TAG, e.message.orEmpty())
            when (mode) {
                LaunchMode.REAL_TAP -> LaunchResult.NoHandler
                LaunchMode.OPEN_IN_APP -> LaunchResult.NotHandledByApp
            }
        } catch (e: RuntimeException) {
            LaunchResult.Failed(failureReason(e))
        }

    /** The exception's simple class name, followed by its message only when there is one. */
    private fun failureReason(error: RuntimeException): String {
        val name = error::class.java.simpleName
        val message = error.message
        return if (message.isNullOrBlank()) name else "$name: $message"
    }

    private fun intent(
        link: String,
        mode: LaunchMode,
    ): Intent =
        Intent(Intent.ACTION_VIEW, link.toUri()).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            when (mode) {
                LaunchMode.REAL_TAP -> addCategory(Intent.CATEGORY_BROWSABLE)
                LaunchMode.OPEN_IN_APP -> setPackage(activity.packageName)
            }
        }

    private companion object {
        const val TAG = "DeepLinkLauncher"
    }
}
