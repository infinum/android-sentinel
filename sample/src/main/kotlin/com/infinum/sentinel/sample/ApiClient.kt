package com.infinum.sentinel.sample

import android.content.Context
import com.infinum.sentinel.domain.networkemuator.NetworkEmulatorInterceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Simple API client for testing the Network Emulator tool.
 * Demonstrates how to integrate NetworkEmulatorInterceptor with OkHttp.
 */
object ApiClient {
    private lateinit var client: OkHttpClient

    fun initialize(context: Context) {
        client = OkHttpClient.Builder()
            .callTimeout(timeout = 10L, unit = TimeUnit.SECONDS)
            .addInterceptor(NetworkEmulatorInterceptor(context)) // todo how to disable this for release?
            .build()
    }

    /**
     * Make a test API call to fetch request details.
     */
    @Throws(IOException::class)
    fun fetchRequestDetails(): String {
        val request = Request.Builder()
            .url("https://httpbin.org/get")
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("HTTP ${response.code}: ${response.message}")
            }
            return response.body?.string() ?: "Empty response"
        }
    }
}
