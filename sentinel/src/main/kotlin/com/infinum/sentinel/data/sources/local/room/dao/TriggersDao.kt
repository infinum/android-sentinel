package com.infinum.sentinel.data.sources.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.infinum.sentinel.data.models.local.TriggerEntity

@Dao
internal interface TriggersDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(entity: TriggerEntity)

    @Query("SELECT * FROM triggers")
    suspend fun load(): List<TriggerEntity>

    @Query("SELECT * FROM triggers WHERE type = 'FOREGROUND'")
    suspend fun foreground(): TriggerEntity
}
