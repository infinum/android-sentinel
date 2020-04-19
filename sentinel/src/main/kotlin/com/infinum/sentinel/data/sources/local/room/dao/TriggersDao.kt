package com.infinum.sentinel.data.sources.local.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.infinum.sentinel.data.models.local.TriggerEntity

@Dao
internal interface TriggersDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(entity: TriggerEntity)

    @Query("SELECT * FROM triggers")
    fun load(): LiveData<List<TriggerEntity>>
}
