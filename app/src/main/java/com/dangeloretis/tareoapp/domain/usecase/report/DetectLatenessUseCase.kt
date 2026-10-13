package com.dangeloretis.tareoapp.domain.usecase.report

import com.dangeloretis.tareoapp.domain.model.Attendance
import com.dangeloretis.tareoapp.domain.model.AttendanceType
import com.dangeloretis.tareoapp.domain.model.WorkSchedule
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class DetectLatenessUseCase @Inject constructor() {
    operator fun invoke(attendances: List<Attendance>, date: Instant, zoneId: ZoneId = ZoneId.systemDefault()): Long {
        val targetDate = date.atZone(zoneId).toLocalDate()
        val firstEntry = attendances
            .filter { it.type == AttendanceType.CHECK_IN }
            .filter { Instant.ofEpochMilli(it.deviceTimeMillis).atZone(zoneId).toLocalDate() == targetDate }
            .minByOrNull { it.deviceTimeMillis }
            ?: return 0L

        val entryTime = Instant.ofEpochMilli(firstEntry.deviceTimeMillis).atZone(zoneId).toLocalTime()
        val expectedEntryTime = WorkSchedule.ENTRY_TIME.plusMinutes(WorkSchedule.TOLERANCE_MINUTES)
        
        return if (entryTime.isAfter(expectedEntryTime)) {
            ChronoUnit.MINUTES.between(WorkSchedule.ENTRY_TIME, entryTime)
        } else {
            0L
        }
    }
}
