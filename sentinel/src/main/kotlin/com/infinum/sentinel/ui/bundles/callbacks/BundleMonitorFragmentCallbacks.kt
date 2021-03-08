package com.infinum.sentinel.ui.bundles.callbacks

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

internal class BundleMonitorFragmentCallbacks(
    private val onBundleLogged: (Long, String?, BundleCallSite, Bundle) -> Unit
) : FragmentManager.FragmentLifecycleCallbacks() {

    override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
        f.arguments?.let {
            onBundleLogged(
                System.currentTimeMillis(),
                f::class.simpleName,
                BundleCallSite.FRAGMENT_ARGUMENTS,
                it
            )
        }
    }

    override fun onFragmentSaveInstanceState(fm: FragmentManager, f: Fragment, outState: Bundle) =
        onBundleLogged(
            System.currentTimeMillis(),
            f::class.simpleName,
            BundleCallSite.FRAGMENT_SAVED_STATE,
            outState
        )
}
