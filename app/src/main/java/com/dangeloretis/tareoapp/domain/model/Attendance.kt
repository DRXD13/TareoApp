package com.dangeloretis.tareoapp.domain.model

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
    val distanceMeters: Float
)
