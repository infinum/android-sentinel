package com.infinum.sentinel.domain.formats.models

import com.infinum.sentinel.data.models.local.FormatEntity
import com.infinum.sentinel.domain.shared.base.BaseParameters

internal data class FormatsParameters(
    val entities: List<FormatEntity>? = null
) : BaseParameters
