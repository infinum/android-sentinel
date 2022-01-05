package com.infinum.sentinel.domain.crash.models

import com.infinum.sentinel.domain.shared.base.BaseParameters

internal data class CrashParameters(
    val query: String? = null,
    val crashId: Long? = null,
) : BaseParameters
