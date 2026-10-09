package com.dangeloretis.tareoapp.domain.model

import com.dangeloretis.tareoapp.data.local.entity.SyncStatus

data class Attendance(
    val id: String,
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
    val syncStatus: SyncStatus = SyncStatus.PENDING
)
