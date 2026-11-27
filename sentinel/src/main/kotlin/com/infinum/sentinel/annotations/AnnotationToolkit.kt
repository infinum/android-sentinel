package com.infinum.sentinel.annotations

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The UI toolkit an annotated element belongs to.
 */
@Serializable
internal enum class AnnotationToolkit {
    @SerialName("view")
    VIEW,

    @SerialName("compose")
    COMPOSE,
}
