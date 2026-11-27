package com.infinum.sentinel.data.sources.raw.deeplinks

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.infinum.sentinel.domain.deeplinks.DeepLinksCollectors

/**
 * Reads the runtime enabled state of this app's components from the package manager. Never throws.
 */
internal class ComponentStatesCollector(
    private val context: Context,
) : DeepLinksCollectors.ComponentStates {
    override fun runtimeEnabled(componentName: String): Boolean? =
        try {
            when (context.packageManager.getComponentEnabledSetting(ComponentName(context.packageName, componentName))) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> true
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED,
                -> false
                // COMPONENT_ENABLED_STATE_DEFAULT: the manifest decides.
                else -> null
            }
        } catch (ignored: RuntimeException) {
            // An unknown component (IllegalArgumentException) or a failed binder call: leave the manifest's value,
            // so one missing chip never fails the whole discovery.
            null
        }
}
