package com.infinum.sentinel.domain.deeplinks.variants

/**
 * Thrown by [DeepLinkVariantsRepository] when the variants could not be written to storage.
 *
 * It deliberately does not extend [IllegalStateException], which is the supertype of
 * `CancellationException`, so callers can catch it without swallowing coroutine cancellation.
 */
internal class VariantsWriteException : RuntimeException("Failed to persist deep link variants")
