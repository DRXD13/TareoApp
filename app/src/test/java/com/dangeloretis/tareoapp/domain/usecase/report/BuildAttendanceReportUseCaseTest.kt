package com.dangeloretis.tareoapp.domain.usecase.report

import com.dangeloretis.tareoapp.domain.model.Worker
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class BuildAttendanceReportUseCaseTest {

    private val calculateWorkedHoursUseCase = CalculateWorkedHoursUseCase()
    private val detectLatenessUseCase = DetectLatenessUseCase()

    private val worker = Worker("11111111", "Test Worker")

    @Test
    fun `sunday is not workable`() {
        // Oct 15 2023 is a Sunday. Clock is on that day, 10:00 AM.
        val clock = Clock.fixed(Instant.parse("2023-10-15T10:00:00Z"), ZoneId.of("UTC"))
        val useCase = BuildAttendanceReportUseCase(calculateWorkedHoursUseCase, detectLatenessUseCase, clock)

        val result = useCase(
            workers = listOf(worker),
            attendances = emptyList(),
            startDate = Instant.parse("2023-10-15T00:00:00Z"),
            endDate = Instant.parse("2023-10-15T23:59:59Z"),
            zoneId = ZoneId.of("UTC")
        )

        val report = result.first()
        assertEquals(0, report.daysAttended)
        assertEquals(0, report.absences)
        assertEquals(0, report.dailyReports.size)
    }

    @Test
    fun `current day before entry time is pending`() {
        // Monday Oct 16 2023, Clock is 08:05 AM (Tolerance is until 08:10 AM)
        val clock = Clock.fixed(Instant.parse("2023-10-16T08:05:00Z"), ZoneId.of("UTC"))
        val useCase = BuildAttendanceReportUseCase(calculateWorkedHoursUseCase, detectLatenessUseCase, clock)

        val result = useCase(
            workers = listOf(worker),
            attendances = emptyList(),
            startDate = Instant.parse("2023-10-16T00:00:00Z"),
            endDate = Instant.parse("2023-10-16T23:59:59Z"),
            zoneId = ZoneId.of("UTC")
        )

        val report = result.first()
        assertEquals(0, report.daysAttended)
        assertEquals(0, report.absences) // Not an absence yet
        assertEquals(0, report.dailyReports.size)
    }

    @Test
    fun `current day after entry time is absence`() {
        // Monday Oct 16 2023, Clock is 08:15 AM (Past tolerance 08:10)
        val clock = Clock.fixed(Instant.parse("2023-10-16T08:15:00Z"), ZoneId.of("UTC"))
        val useCase = BuildAttendanceReportUseCase(calculateWorkedHoursUseCase, detectLatenessUseCase, clock)

        val result = useCase(
            workers = listOf(worker),
            attendances = emptyList(),
            startDate = Instant.parse("2023-10-16T00:00:00Z"),
            endDate = Instant.parse("2023-10-16T23:59:59Z"),
            zoneId = ZoneId.of("UTC")
        )

        val report = result.first()
        assertEquals(0, report.daysAttended)
        assertEquals(1, report.absences)
        assertEquals(1, report.dailyReports.size)
        assertEquals(DailyState.FALTA, report.dailyReports.first().state)
    }
}
