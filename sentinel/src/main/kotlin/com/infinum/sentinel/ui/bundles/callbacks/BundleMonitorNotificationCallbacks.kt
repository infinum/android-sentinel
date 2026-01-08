package com.infinum.sentinel.ui.bundles.callbacks

import android.app.Activity
import android.app.Application
import android.os.Bundle

internal class BundleMonitorNotificationCallbacks : Application.ActivityLifecycleCallbacks {
    var currentActivity: Activity? = null

    override fun onActivityCreated(
        activity: Activity,
        savedInstanceState: Bundle?,
    ) {
        currentActivity = activity
    }

    override fun onActivityStarted(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityResumed(activity: Activity) {
        currentActivity = activity
    }

    override fun onActivityPaused(activity: Activity) = Unit

    override fun onActivityDestroyed(activity: Activity) = Unit

    override fun onActivityStopped(activity: Activity) = Unit

    override fun onActivitySaveInstanceState(
        activity: Activity,
        outState: Bundle,
    ) = Unit
}
