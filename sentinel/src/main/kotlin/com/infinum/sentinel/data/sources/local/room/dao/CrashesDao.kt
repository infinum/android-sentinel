package com.infinum.sentinel.data.sources.local.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.infinum.sentinel.data.models.local.CrashEntity
import kotlinx.coroutines.flow.Flow

@Dao
internal interface CrashesDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(entity: CrashEntity): Long

    @Query("SELECT * FROM crashes where id is :id")
    suspend fun loadById(id: Long): CrashEntity

    @Query("SELECT * FROM crashes order by timestamp DESC")
    fun loadAll(): Flow<List<CrashEntity>>

    @Query("DELETE FROM crashes where id is :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM crashes")
    suspend fun deleteAll()
}
