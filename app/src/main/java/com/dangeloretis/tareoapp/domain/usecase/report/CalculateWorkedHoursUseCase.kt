package com.dangeloretis.tareoapp.domain.usecase.report

import com.dangeloretis.tareoapp.domain.model.Attendance
import com.dangeloretis.tareoapp.domain.model.AttendanceType
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class CalculateWorkedHoursUseCase @Inject constructor() {
    operator fun invoke(attendances: List<Attendance>, date: Instant, zoneId: ZoneId = ZoneId.systemDefault()): WorkedHoursResult {
        val targetDate = date.atZone(zoneId).toLocalDate()
        val dayAttendances = attendances
            .filter { Instant.ofEpochMilli(it.deviceTimeMillis).atZone(zoneId).toLocalDate() == targetDate }
            .distinctBy { Pair(it.type, it.deviceTimeMillis) }
            .sortedBy { it.deviceTimeMillis }

        if (dayAttendances.isEmpty()) return WorkedHoursResult(0.0, 0L, false)

        var totalMinutes = 0L
        var isIncomplete = false
        var currentEntry: Attendance? = null

        for (attendance in dayAttendances) {
            if (attendance.type == AttendanceType.CHECK_IN) {
                if (currentEntry != null) {
                    isIncomplete = true
                }
                currentEntry = attendance
            } else if (attendance.type == AttendanceType.CHECK_OUT) {
                if (currentEntry != null) {
                    val entryTime = Instant.ofEpochMilli(currentEntry.deviceTimeMillis)
                    val exitTime = Instant.ofEpochMilli(attendance.deviceTimeMillis)
                    val minutes = ChronoUnit.MINUTES.between(entryTime, exitTime)
                    totalMinutes += minutes
                    currentEntry = null
                }
            }
        }

        if (currentEntry != null) {
            isIncomplete = true
        }

        return WorkedHoursResult(totalMinutes / 60.0, totalMinutes, isIncomplete)
    }
}

data class WorkedHoursResult(
    val hours: Double,
    val minutes: Long,
    val isIncomplete: Boolean
)
