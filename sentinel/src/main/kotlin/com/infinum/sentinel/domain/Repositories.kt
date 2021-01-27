package com.infinum.sentinel.domain

import com.infinum.sentinel.data.models.local.FormatEntity
import com.infinum.sentinel.data.models.local.TriggerEntity
import com.infinum.sentinel.domain.formats.models.FormatsParameters
import com.infinum.sentinel.domain.shared.base.BaseRepository
import com.infinum.sentinel.domain.triggers.models.TriggerParameters

internal interface Repositories {

    interface Triggers : BaseRepository<TriggerParameters, List<TriggerEntity>>

    interface Formats : BaseRepository<FormatsParameters, FormatEntity>
}
