package com.infinum.sentinel.domain

import com.infinum.sentinel.data.models.local.BundleMonitorEntity
import com.infinum.sentinel.data.models.local.CertificateMonitorEntity
import com.infinum.sentinel.data.models.local.CrashMonitorEntity
import com.infinum.sentinel.data.models.local.FormatEntity
import com.infinum.sentinel.data.models.local.TriggerEntity
import com.infinum.sentinel.data.models.raw.CertificateData
import com.infinum.sentinel.data.models.raw.PreferenceType
import com.infinum.sentinel.domain.bundle.descriptor.models.BundleDescriptor
import com.infinum.sentinel.domain.bundle.descriptor.models.BundleParameters
import com.infinum.sentinel.domain.bundle.monitor.models.BundleMonitorParameters
import com.infinum.sentinel.domain.certificate.models.CertificateParameters
import com.infinum.sentinel.domain.certificate.monitor.models.CertificateMonitorParameters
import com.infinum.sentinel.domain.crash.monitor.models.CrashMonitorParameters
import com.infinum.sentinel.domain.formats.models.FormatsParameters
import com.infinum.sentinel.domain.preference.models.PreferenceParameters
import com.infinum.sentinel.domain.shared.base.BaseRepository
import com.infinum.sentinel.domain.triggers.models.TriggerParameters

internal interface Repositories {

    interface Triggers : BaseRepository<TriggerParameters, List<TriggerEntity>>

    interface Formats : BaseRepository<FormatsParameters, FormatEntity>

    interface BundleMonitor : BaseRepository<BundleMonitorParameters, BundleMonitorEntity>

    interface Bundles : BaseRepository<BundleParameters, List<BundleDescriptor>> {

        suspend fun clear()
    }

    interface Preference : BaseRepository<PreferenceParameters, Unit> {

        fun cache(cache: PreferenceParameters.Cache)

        fun consume(): Pair<String, Triple<PreferenceType, String, Any>>
    }

    interface TargetedPreferences : BaseRepository<PreferenceParameters, Unit> {

        fun cache(cache: PreferenceParameters.Cache)

        fun consume(): Pair<String, Triple<PreferenceType, String, Any>>
    }

    interface CrashMonitor : BaseRepository<CrashMonitorParameters, CrashMonitorEntity>

    interface Certificate : BaseRepository<CertificateParameters, Unit> {

        fun cache(cache: CertificateParameters.Cache)

        fun consume(): CertificateData
    }

    interface CertificateMonitor : BaseRepository<CertificateMonitorParameters, CertificateMonitorEntity>
}
