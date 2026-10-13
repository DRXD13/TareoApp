package com.dangeloretis.tareoapp.domain.usecase.report

import com.dangeloretis.tareoapp.domain.model.Attendance
import com.dangeloretis.tareoapp.domain.model.AttendanceType
import com.dangeloretis.tareoapp.data.local.entity.SyncStatus
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class DetectAbsencesUseCaseTest {
    private val detectAbsencesUseCase = DetectAbsencesUseCase()

    private fun createAttendance(type: AttendanceType, timeMillis: Long): Attendance {
        return Attendance(
            id = "id_$timeMillis",
            userId = "1",
            siteId = "site1",
            type = type,
            latitude = 0.0,
            longitude = 0.0,
            accuracyMeters = 0f,
            deviceTimeMillis = timeMillis,
            elapsedRealtimeMillis = 0L,
            isMockLocation = false,
            distanceMeters = 0f,
            syncStatus = SyncStatus.SYNCED
        )
    }

    @Test
    fun `absent on weekday`() {
        // Oct 10 2023 is a Tuesday
        val date = Instant.parse("2023-10-10T00:00:00Z")
        val zoneId = ZoneId.of("UTC")
        
        val result = detectAbsencesUseCase(emptyList(), date, zoneId)
        
        assertEquals(true, result)
    }

    @Test
    fun `not absent on sunday`() {
        // Oct 15 2023 is a Sunday
        val date = Instant.parse("2023-10-15T00:00:00Z")
        val zoneId = ZoneId.of("UTC")
        
        val result = detectAbsencesUseCase(emptyList(), date, zoneId)
        
        assertEquals(false, result)
    }
}