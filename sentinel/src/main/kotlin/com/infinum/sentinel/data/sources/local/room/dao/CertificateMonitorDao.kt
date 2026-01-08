package com.infinum.sentinel.data.sources.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.infinum.sentinel.data.models.local.CertificateMonitorEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface CertificateMonitorDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(entity: CertificateMonitorEntity)

    @Query("SELECT * FROM certificate_monitor LIMIT 1")
    fun load(): Flow<CertificateMonitorEntity>
}
