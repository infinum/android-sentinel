package com.infinum.sentinel.ui.deeplinks.launch

/**
 * Outcome of [DeepLinkLauncher.launch]. The launcher never throws, so every failure is a value here.
 */
internal sealed interface LaunchResult {
    /** The system accepted the intent and started an activity. */
    data object Launched : LaunchResult

    /** Real tap mode: no app on the device can open the link. */
    data object NoHandler : LaunchResult

    /** Open in this app mode: this app has no activity that handles the link. */
    data object NotHandledByApp : LaunchResult

    /** The launch failed for another reason, for example a [SecurityException]. [reason] is ready to show. */
    data class Failed(val reason: String) : LaunchResult
}
