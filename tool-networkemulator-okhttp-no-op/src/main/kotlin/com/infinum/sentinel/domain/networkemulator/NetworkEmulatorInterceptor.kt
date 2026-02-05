package com.infinum.sentinel.domain.networkemulator

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

/**
 * No-op implementation of Network Emulator Interceptor.
 *
 * This implementation does nothing and simply passes requests through without any delays,
 * failures, or modifications. It is safe to include in release builds.
 *
 * Usage:
 * ```
 * val client = OkHttpClient.Builder()
 *     .addInterceptor(NetworkEmulatorInterceptor(context))
 *     .build()
 * ```
 *
 * This class maintains API compatibility with the debug implementation while having
 * zero runtime overhead.
 */
public class NetworkEmulatorInterceptor(
    @Suppress("UNUSED_PARAMETER") context: Context,
) : Interceptor {
    /**
     * No-op intercept implementation that simply proceeds with the request without
     * any network emulation.
     */
    override fun intercept(chain: Interceptor.Chain): Response = chain.proceed(chain.request())
}
