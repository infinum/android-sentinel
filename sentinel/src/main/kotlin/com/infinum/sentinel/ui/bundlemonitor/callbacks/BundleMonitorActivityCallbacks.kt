package com.infinum.sentinel.ui.bundlemonitor.callbacks

import android.app.Activity
import android.app.Application
import android.os.Bundle
import androidx.fragment.app.FragmentActivity

internal class BundleMonitorActivityCallbacks(
    private val onBundleLogged: (Long, String?, BundleCallSite, Bundle) -> Unit
) : Application.ActivityLifecycleCallbacks {

    private val fragmentCallbacks = BundleMonitorFragmentCallbacks(onBundleLogged)

    override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        if (activity is FragmentActivity) {
            activity.supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentCallbacks, true)
        }
        activity.intent.extras?.let {
            onBundleLogged(
                System.currentTimeMillis(),
                activity::class.simpleName,
                BundleCallSite.ACTIVITY_INTENT_EXTRAS,
                it
            )
        }
    }

    override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) =
        onBundleLogged(
            System.currentTimeMillis(),
            activity::class.simpleName,
            BundleCallSite.ACTIVITY_SAVED_STATE,
            outState
        )

    override fun onActivityStarted(activity: Activity) = Unit

    override fun onActivityResumed(activity: Activity) = Unit

    override fun onActivityPaused(activity: Activity) = Unit

    override fun onActivityStopped(activity: Activity) = Unit

    override fun onActivityDestroyed(activity: Activity) = Unit
}