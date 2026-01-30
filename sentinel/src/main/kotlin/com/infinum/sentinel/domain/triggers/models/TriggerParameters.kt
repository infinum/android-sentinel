package com.infinum.sentinel.domain.triggers.models

import com.infinum.sentinel.data.models.local.TriggerEntity
import com.infinum.sentinel.domain.shared.base.BaseParameters

internal data class TriggerParameters(
    val entity: TriggerEntity? = null,
) : BaseParameters
