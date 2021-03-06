package com.infinum.sentinel.domain

import com.infinum.sentinel.data.models.local.BundleMonitorEntity
import com.infinum.sentinel.data.models.local.FormatEntity
import com.infinum.sentinel.data.models.local.TriggerEntity
import com.infinum.sentinel.domain.bundle.descriptor.models.BundleDescriptor
import com.infinum.sentinel.domain.bundle.descriptor.models.BundleParameters
import com.infinum.sentinel.domain.bundle.monitor.models.BundleMonitorParameters
import com.infinum.sentinel.domain.formats.models.FormatsParameters
import com.infinum.sentinel.domain.shared.base.BaseRepository
import com.infinum.sentinel.domain.triggers.models.TriggerParameters
import kotlinx.coroutines.flow.Flow

internal interface Repositories {

    interface Triggers : BaseRepository<TriggerParameters, List<TriggerEntity>>

    interface Formats : BaseRepository<FormatsParameters, FormatEntity>

    interface BundleMonitor : BaseRepository<BundleMonitorParameters, BundleMonitorEntity>

    interface Bundles : BaseRepository<BundleParameters, BundleDescriptor>
}
