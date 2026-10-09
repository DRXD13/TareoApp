package com.dangeloretis.tareoapp.data.mapper

import com.dangeloretis.tareoapp.data.local.entity.SyncStatus
import com.dangeloretis.tareoapp.domain.model.Attendance
import com.dangeloretis.tareoapp.domain.model.AttendanceType
import org.junit.Assert.assertEquals
import org.junit.Test

class AttendanceMapperTest {

    @Test
    fun `map toEntity preserves data and adds syncStatus`() {
        val attendance = Attendance(
            id = "1",
            userId = "user1",
            siteId = "site1",
            type = AttendanceType.CHECK_IN,
            latitude = 10.0,
            longitude = 20.0,
            accuracyMeters = 5f,
            deviceTimeMillis = 1000L,
            elapsedRealtimeMillis = 2000L,
            isMockLocation = false,
            distanceMeters = 50f
        )

        val entity = attendance.toEntity(syncStatusOverride = SyncStatus.PENDING)

        assertEquals("1", entity.id)
        assertEquals("user1", entity.userId)
        assertEquals(SyncStatus.PENDING, entity.syncStatus)
        assertEquals(null, entity.syncedAtMillis)
        assertEquals(50f, entity.distanceMeters)
    }

    @Test
    fun `map toDomain preserves data`() {
        val entity = com.dangeloretis.tareoapp.data.local.entity.AttendanceEntity(
            id = "2",
            userId = "user2",
            siteId = "site2",
            type = AttendanceType.CHECK_OUT,
            latitude = 15.0,
            longitude = 25.0,
            accuracyMeters = 10f,
            deviceTimeMillis = 3000L,
            elapsedRealtimeMillis = 4000L,
            isMockLocation = true,
            distanceMeters = 100f,
            syncStatus = SyncStatus.SYNCED,
            syncedAtMillis = 5000L
        )

        val attendance = entity.toDomain()

        assertEquals("2", attendance.id)
        assertEquals("user2", attendance.userId)
        assertEquals(AttendanceType.CHECK_OUT, attendance.type)
        assertEquals(100f, attendance.distanceMeters)
        assertEquals(SyncStatus.SYNCED, attendance.syncStatus)
    }
}
