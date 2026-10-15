package com.infinum.sentinel.domain.networkproxy

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

public class NetworkProxyInterceptor(
    @Suppress("UNUSED_PARAMETER") context: Context,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response = chain.proceed(chain.request())
}
