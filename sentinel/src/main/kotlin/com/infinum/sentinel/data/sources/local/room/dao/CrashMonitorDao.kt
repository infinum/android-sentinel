package com.infinum.sentinel.data.sources.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.infinum.sentinel.data.models.local.CrashMonitorEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface CrashMonitorDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(entity: CrashMonitorEntity)

    @Query("SELECT * FROM crash_monitor LIMIT 1")
    fun load(): Flow<CrashMonitorEntity>
}
