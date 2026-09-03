package com.infinum.sentinel.annotations

import android.content.Context
import com.infinum.sentinel.R
import com.infinum.sentinel.domain.annotations.HttpPoster
import com.infinum.sentinel.domain.annotations.UrlConnectionHttpPoster
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Fires a Claude Code routine, sending the annotation JSON as an extra turn appended to
 * the routine's session.
 *
 * [token] supplies a Claude Code OAuth token, used as a bearer token. It is a lambda rather
 * than a string so an app can read it from `BuildConfig` or a development-only source instead
 * of hardcoding a credential that ships inside the APK. [fireUrl] is the routine's fire
 * endpoint, for example
 * `https://api.anthropic.com/v1/claude_code/routines/<trigger-id>/fire`.
 */
public class ClaudeRoutineExporter internal constructor(
    private val token: () -> String?,
    private val fireUrl: String,
    private val poster: HttpPoster,
) : AnnotationExporter {
    public constructor(token: () -> String?, fireUrl: String) : this(token, fireUrl, UrlConnectionHttpPoster())

    private companion object {
        private const val ANTHROPIC_VERSION = "2023-06-01"
        private const val ANTHROPIC_BETA = "experimental-cc-routine-2026-04-01"
        private const val SUCCESS_RANGE_START = 200
        private const val SUCCESS_RANGE_END = 299
    }

    override fun icon(): Int = R.drawable.sentinel_ic_send

    override fun label(): Int = R.string.sentinel_annotations_send

    override suspend fun export(context: Context, annotations: List<SentinelAnnotation>): String {
        val count = send(annotations)
        return context.getString(R.string.sentinel_annotations_sent, count)
    }

    /** Posts [annotations] and returns how many were sent. Throws [IllegalStateException] on failure. */
    internal suspend fun send(annotations: List<SentinelAnnotation>): Int {
        val bearer = token()
        check(!bearer.isNullOrBlank()) { "No Claude token configured" }

        val payload =
            Json.encodeToString(
                JsonObject.serializer(),
                JsonObject(mapOf("text" to JsonPrimitive(SentinelAnnotation.toExportJson(annotations)))),
            )

        val response =
            poster.post(
                url = fireUrl,
                headers =
                mapOf(
                    "Authorization" to "Bearer $bearer",
                    "anthropic-version" to ANTHROPIC_VERSION,
                    "anthropic-beta" to ANTHROPIC_BETA,
                    "Content-Type" to "application/json",
                ),
                body = payload,
            )

        check(response.code in SUCCESS_RANGE_START..SUCCESS_RANGE_END) {
            "Failed to send annotations to Claude (HTTP ${response.code}): ${response.body}"
        }

        return annotations.size
    }
}
