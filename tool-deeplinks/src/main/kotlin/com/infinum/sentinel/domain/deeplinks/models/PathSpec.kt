package com.infinum.sentinel.domain.deeplinks.models

/**
 * The path part of a declared link, one variant per manifest path attribute.
 */
internal sealed interface PathSpec {
    /** No path declared. */
    data object None : PathSpec

    /** `android:path`: exact match. [value] is plain (decoded) text. */
    data class Literal(val value: String) : PathSpec

    /** `android:pathPrefix`: matches paths starting with [value]. */
    data class Prefix(val value: String) : PathSpec

    /** `android:pathPattern`: simple glob pattern. */
    data class Pattern(val value: String) : PathSpec

    /** `android:pathAdvancedPattern`: regular expression like pattern (Android 12+). */
    data class AdvancedPattern(val value: String) : PathSpec

    /** `android:pathSuffix`: matches paths ending with [value] (Android 12+). */
    data class Suffix(val value: String) : PathSpec
}
