package com.infinum.sentinel.data.sources.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.infinum.sentinel.data.models.local.BundleMonitorEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface BundleMonitorDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(entity: BundleMonitorEntity)

    @Query("SELECT * FROM bundle_monitor LIMIT 1")
    fun load(): Flow<BundleMonitorEntity>
}
