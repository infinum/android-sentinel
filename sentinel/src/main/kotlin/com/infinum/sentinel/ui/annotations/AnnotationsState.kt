package com.infinum.sentinel.ui.annotations

import com.infinum.sentinel.data.models.local.annotations.Annotation

internal sealed class AnnotationsState {
    data class Data(
        val annotations: List<Annotation>,
    ) : AnnotationsState()
}
