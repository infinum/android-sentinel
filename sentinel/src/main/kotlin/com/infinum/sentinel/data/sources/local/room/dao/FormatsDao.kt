package com.infinum.sentinel.data.sources.local.room.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.infinum.sentinel.data.models.local.FormatEntity

@Dao
internal interface FormatsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun save(entities: List<FormatEntity>)

    @Query("SELECT * FROM formats WHERE selected = 1")
    fun load(): LiveData<FormatEntity>
}
