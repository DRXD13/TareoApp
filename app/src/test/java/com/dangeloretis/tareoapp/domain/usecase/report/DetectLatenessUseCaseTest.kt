package com.dangeloretis.tareoapp.domain.usecase.report

import com.dangeloretis.tareoapp.domain.model.Attendance
import com.dangeloretis.tareoapp.domain.model.AttendanceType
import com.dangeloretis.tareoapp.data.local.entity.SyncStatus
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class DetectLatenessUseCaseTest {
    private val detectLatenessUseCase = DetectLatenessUseCase()

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
    fun `punctual entry`() {
        val date = Instant.parse("2023-10-10T00:00:00Z")
        val zoneId = ZoneId.of("UTC")
        val entryTime = Instant.parse("2023-10-10T08:05:00Z").toEpochMilli()

        val attendances = listOf(createAttendance(AttendanceType.CHECK_IN, entryTime))

        val result = detectLatenessUseCase(attendances, date, zoneId)

        assertEquals(0L, result)
    }

    @Test
    fun `late entry`() {
        val date = Instant.parse("2023-10-10T00:00:00Z")
        val zoneId = ZoneId.of("UTC")
        val entryTime = Instant.parse("2023-10-10T08:15:00Z").toEpochMilli()

        val attendances = listOf(createAttendance(AttendanceType.CHECK_IN, entryTime))

        val result = detectLatenessUseCase(attendances, date, zoneId)

        assertEquals(15L, result)
    }
}