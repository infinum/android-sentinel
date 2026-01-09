package com.infinum.sentinel.domain.networkemuator

import android.content.Context
import kotlin.random.Random
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody

/**
 * OkHttp interceptor that emulates slow and flaky network conditions.
 *
 * This interceptor can:
 * - Add configurable delays to network requests
 * - Simulate request failures based on a failure percentage
 * - Add variance to delay times to simulate unstable connections
 *
 * Usage:
 * ```
 * val client = OkHttpClient.Builder()
 *     .addInterceptor(NetworkEmulatorInterceptor(context))
 *     .build()
 * ```
 *
 * Configure the behavior through NetworkEmulatorPreferences or the NetworkEmulatorTool UI.
 */
public class NetworkEmulatorInterceptor(
    context: Context,
) : Interceptor {
    private val preferences = NetworkEmulatorPreferences(context.applicationContext)

    override fun intercept(chain: Interceptor.Chain): Response {
        // If emulation is disabled, proceed normally
        if (!preferences.isEnabled) {
            return chain.proceed(chain.request())
        }

        // Calculate delay with variance
        val baseDelay = preferences.delayMs
        val variance = preferences.variancePercentage
        val actualDelay = calculateDelayWithVariance(baseDelay, variance)

        // Apply the delay
        if (actualDelay > 0) {
            Thread.sleep(actualDelay.toLong())
        }

        // Check if this request should fail
        val failPercentage = preferences.failPercentage
        if (shouldFail(failPercentage)) {
            return createFailureResponse(chain)
        }

        // Proceed with the actual request
        return chain.proceed(chain.request())
    }

    /**
     * Calculate delay with variance applied.
     * Variance adds randomness to the delay: ±variance% of the base delay.
     */
    private fun calculateDelayWithVariance(
        baseDelay: Int,
        variancePercent: Int,
    ): Int {
        if (variancePercent == 0) {
            return baseDelay
        }

        val varianceAmount = (baseDelay * variancePercent) / 100
        val randomVariance = Random.nextInt(-varianceAmount, varianceAmount + 1)
        return (baseDelay + randomVariance).coerceAtLeast(0)
    }

    /**
     * Determine if the current request should fail based on the failure percentage.
     */
    private fun shouldFail(failPercentage: Int): Boolean {
        if (failPercentage <= 0) return false
        if (failPercentage >= 100) return true
        return Random.nextInt(100) < failPercentage
    }

    /**
     * Create a failure response to simulate network errors.
     */
    private fun createFailureResponse(chain: Interceptor.Chain): Response {
        val request = chain.request()
        return Response
            .Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(500)
            .message("Network Emulator: Simulated failure")
            .body(
                """
                {
                    "error": "Simulated network failure",
                    "message": "This request was intentionally failed by Sentinel Network Emulator"
                }
                """.trimIndent().toResponseBody("application/json".toMediaTypeOrNull()),
            ).build()
    }
}
