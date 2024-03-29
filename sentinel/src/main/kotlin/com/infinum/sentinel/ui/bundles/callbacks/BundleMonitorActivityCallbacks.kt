package com.infinum.sentinel.ui.bundles.callbacks

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.infinum.sentinel.extensions.isMonitoredScreen

internal class BundleMonitorActivityCallbacks(
    private val onBundleLogged: (Activity?, Long, String?, BundleCallSite, Bundle) -> Unit
) : Application.ActivityLifecycleCallbacks {

    private val fragmentCallbacks = BundleMonitorFragmentCallbacks(onBundleLogged)

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (activity is FragmentActivity) {
            activity.supportFragmentManager.registerFragmentLifecycleCallbacks(
                fragmentCallbacks,
                true
            )
        }

        if (activity.isMonitoredScreen) {
            activity.intent.extras?.let {
                onBundleLogged(
                    activity,
                    System.currentTimeMillis(),
                    activity::class.simpleName,
                    BundleCallSite.ACTIVITY_INTENT_EXTRAS,
                    it
                )
            }
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) =
        if (activity.isMonitoredScreen) {
            onBundleLogged(
                activity,
                System.currentTimeMillis(),
                activity::class.simpleName,
                BundleCallSite.ACTIVITY_SAVED_STATE,
                outState
            )
        } else {
            @Suppress("RedundantUnitExpression")
            Unit
        }

    override fun onActivityStarted(activity: Activity) = Unit

    override fun onActivityResumed(activity: Activity) = Unit

    override fun onActivityPaused(activity: Activity) = Unit

    override fun onActivityStopped(activity: Activity) = Unit

    override fun onActivityDestroyed(activity: Activity) = Unit
}
