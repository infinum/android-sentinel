package com.infinum.sentinel.data.sources.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.infinum.sentinel.data.models.local.TriggerEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface TriggersDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(entity: TriggerEntity)

    @Query("SELECT * FROM triggers")
    fun load(): Flow<List<TriggerEntity>>
}
