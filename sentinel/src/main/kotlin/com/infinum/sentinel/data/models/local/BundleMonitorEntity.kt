package com.infinum.sentinel.data.models.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bundle_monitor")
internal data class BundleMonitorEntity(

    @PrimaryKey(autoGenerate = false)
    @ColumnInfo(name = "id")
    var id: Long?,

    @ColumnInfo(name = "limit", defaultValue = "500")
    var limit: Int = 500,

    @ColumnInfo(name = "notify", defaultValue = "true")
    var notify: Boolean = true,

    @ColumnInfo(name = "activity_intent_extras", defaultValue = "true")
    var activityIntentExtras: Boolean = true,

    @ColumnInfo(name = "activity_saved_state", defaultValue = "true")
    var activitySavedState: Boolean = true,

    @ColumnInfo(name = "fragment_arguments", defaultValue = "true")
    var fragmentArguments: Boolean = true,

    @ColumnInfo(name = "fragment_saved_state", defaultValue = "true")
    var fragmentSavedState: Boolean = true
)
