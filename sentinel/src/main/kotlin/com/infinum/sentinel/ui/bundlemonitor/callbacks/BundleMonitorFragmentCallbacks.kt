package com.infinum.sentinel.ui.bundlemonitor.callbacks

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager

internal class BundleMonitorFragmentCallbacks(
    private val onBundleLogged: (Bundle) -> Unit
) : FragmentManager.FragmentLifecycleCallbacks() {

    override fun onFragmentCreated(fm: FragmentManager, f: Fragment, savedInstanceState: Bundle?) {
        super.onFragmentCreated(fm, f, savedInstanceState)

        f.arguments?.let { onBundleLogged(it) }
    }

    override fun onFragmentSaveInstanceState(fm: FragmentManager, f: Fragment, outState: Bundle) {
        onBundleLogged(outState)
    }
}