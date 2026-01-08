package com.infinum.sentinel.data.sources.local.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.infinum.sentinel.data.models.local.BundleMonitorEntity
import com.infinum.sentinel.data.models.local.CertificateMonitorEntity
import com.infinum.sentinel.data.models.local.CrashEntity
import com.infinum.sentinel.data.models.local.CrashMonitorEntity
import com.infinum.sentinel.data.models.local.FormatEntity
import com.infinum.sentinel.data.models.local.TriggerEntity
import com.infinum.sentinel.data.sources.local.room.dao.BundleMonitorDao
import com.infinum.sentinel.data.sources.local.room.dao.CertificateMonitorDao
import com.infinum.sentinel.data.sources.local.room.dao.CrashMonitorDao
import com.infinum.sentinel.data.sources.local.room.dao.CrashesDao
import com.infinum.sentinel.data.sources.local.room.dao.FormatsDao
import com.infinum.sentinel.data.sources.local.room.dao.TriggersDao
import com.infinum.sentinel.data.sources.local.room.typeconverters.ChronoUnitConverter
import com.infinum.sentinel.data.sources.local.room.typeconverters.CrashDataConverter
import com.infinum.sentinel.data.sources.local.room.typeconverters.FormatTypeConverter
import com.infinum.sentinel.data.sources.local.room.typeconverters.TriggerTypeConverter
import com.infinum.sentinel.di.LibraryComponents.DATABASE_VERSION

@Database(
    entities = [
        TriggerEntity::class,
        FormatEntity::class,
        BundleMonitorEntity::class,
        CrashEntity::class,
        CrashMonitorEntity::class,
        CertificateMonitorEntity::class,
    ],
    version = DATABASE_VERSION,
    exportSchema = false,
)
@TypeConverters(
    value = [
        TriggerTypeConverter::class,
        FormatTypeConverter::class,
        CrashDataConverter::class,
        ChronoUnitConverter::class,
    ],
)
internal abstract class SentinelDatabase : RoomDatabase() {
    abstract fun triggers(): TriggersDao

    abstract fun formats(): FormatsDao

    abstract fun bundleMonitor(): BundleMonitorDao

    abstract fun crashes(): CrashesDao

    abstract fun crashMonitor(): CrashMonitorDao

    abstract fun certificateMonitor(): CertificateMonitorDao
}
