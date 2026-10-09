package com.dangeloretis.tareoapp.data.mapper

import com.dangeloretis.tareoapp.data.local.entity.AttendanceEntity
import com.dangeloretis.tareoapp.data.local.entity.SyncStatus
import com.dangeloretis.tareoapp.domain.model.Attendance

fun Attendance.toEntity(
    syncStatusOverride: SyncStatus? = null,
    syncedAtMillis: Long? = null
): AttendanceEntity {
    return AttendanceEntity(
        id = id,
        userId = userId,
        siteId = siteId,
        type = type,
        latitude = latitude,
        longitude = longitude,
        accuracyMeters = accuracyMeters,
        deviceTimeMillis = deviceTimeMillis,
        elapsedRealtimeMillis = elapsedRealtimeMillis,
        isMockLocation = isMockLocation,
        distanceMeters = distanceMeters,
        syncStatus = syncStatusOverride ?: syncStatus,
        syncedAtMillis = syncedAtMillis
    )
}

fun AttendanceEntity.toDomain(): Attendance {
    return Attendance(
        id = id,
        userId = userId,
        siteId = siteId,
        type = type,
        latitude = latitude,
        longitude = longitude,
        accuracyMeters = accuracyMeters,
        deviceTimeMillis = deviceTimeMillis,
        elapsedRealtimeMillis = elapsedRealtimeMillis,
        isMockLocation = isMockLocation,
        distanceMeters = distanceMeters,
        syncStatus = syncStatus
    )
}
