package com.infinum.sentinel.data.models.raw.deeplinks

/**
 * Outcome of reading the host app's compiled manifest.
 */
internal sealed interface ManifestReadResult {
    /**
     * The manifest was read and belongs to the host app's base APK.
     */
    data class Success(
        val root: ManifestNodeData,
    ) : ManifestReadResult

    /**
     * The manifest could not be read. The tool then shows only example links and saved variants.
     */
    data class Failure(
        val cause: Throwable,
    ) : ManifestReadResult
}
