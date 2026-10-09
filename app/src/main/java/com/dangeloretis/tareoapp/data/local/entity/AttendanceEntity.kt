package com.dangeloretis.tareoapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dangeloretis.tareoapp.domain.model.AttendanceType

@Entity(tableName = "attendances")
data class AttendanceEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val siteId: String,
    val type: AttendanceType,
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val deviceTimeMillis: Long,
    val elapsedRealtimeMillis: Long,
    val isMockLocation: Boolean,
    val distanceMeters: Float,
    val syncStatus: SyncStatus,
    val syncedAtMillis: Long? = null
)
