package com.dangeloretis.tareoapp.domain.usecase.report

import com.dangeloretis.tareoapp.domain.model.Attendance
import com.dangeloretis.tareoapp.domain.model.AttendanceType
import com.dangeloretis.tareoapp.data.local.entity.SyncStatus
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Instant
import java.time.ZoneId

class CalculateWorkedHoursUseCaseTest {

    private val calculateWorkedHoursUseCase = CalculateWorkedHoursUseCase()

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
    fun `calculate hours with one complete shift`() {
        val date = Instant.parse("2023-10-10T00:00:00Z")
        val zoneId = ZoneId.of("UTC")
        val entryTime = Instant.parse("2023-10-10T08:00:00Z").toEpochMilli()
        val exitTime = Instant.parse("2023-10-10T17:00:00Z").toEpochMilli()

        val attendances = listOf(
            createAttendance(AttendanceType.CHECK_IN, entryTime),
            createAttendance(AttendanceType.CHECK_OUT, exitTime)
        )

        val result = calculateWorkedHoursUseCase(attendances, date, zoneId)

        assertEquals(9.0, result.hours, 0.01)
        assertEquals(false, result.isIncomplete)
    }

    @Test
    fun `calculate hours with incomplete shift`() {
        val date = Instant.parse("2023-10-10T00:00:00Z")
        val zoneId = ZoneId.of("UTC")
        val entryTime = Instant.parse("2023-10-10T08:00:00Z").toEpochMilli()

        val attendances = listOf(
            createAttendance(AttendanceType.CHECK_IN, entryTime)
        )

        val result = calculateWorkedHoursUseCase(attendances, date, zoneId)

        assertEquals(0.0, result.hours, 0.01)
        assertEquals(0L, result.minutes)
        assertEquals(true, result.isIncomplete)
    }
}