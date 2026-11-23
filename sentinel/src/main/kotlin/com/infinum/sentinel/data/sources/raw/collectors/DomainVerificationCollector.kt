package com.infinum.sentinel.data.sources.raw.collectors

import android.content.Context
import android.content.pm.PackageManager
import android.content.pm.verify.domain.DomainVerificationManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.infinum.sentinel.data.models.raw.deeplinks.DomainVerificationData
import com.infinum.sentinel.domain.collectors.Collectors

/**
 * Reads the App Links domain verification state of this app. Returns null below API 31 or when
 * the state is unavailable. Never throws.
 */
internal class DomainVerificationCollector(
    private val context: Context,
) : Collectors.DomainVerification {
    override fun invoke(): DomainVerificationData? =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            null
        } else {
            read()
        }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun read(): DomainVerificationData? =
        try {
            context
                .getSystemService(DomainVerificationManager::class.java)
                ?.getDomainVerificationUserState(context.packageName)
                ?.let { state ->
                    DomainVerificationData(
                        isLinkHandlingAllowed = state.isLinkHandlingAllowed,
                        hostStates = state.hostToStateMap.toMap(),
                    )
                }
        } catch (ignored: PackageManager.NameNotFoundException) {
            null
        }
}
