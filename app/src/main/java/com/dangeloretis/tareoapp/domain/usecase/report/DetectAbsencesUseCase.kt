package com.dangeloretis.tareoapp.domain.usecase.report

import com.dangeloretis.tareoapp.domain.model.Attendance
import com.dangeloretis.tareoapp.domain.model.AttendanceType
import com.dangeloretis.tareoapp.domain.model.WorkSchedule
import java.time.Instant
import java.time.ZoneId
import javax.inject.Inject

class DetectAbsencesUseCase @Inject constructor() {
    operator fun invoke(attendances: List<Attendance>, date: Instant, zoneId: ZoneId = ZoneId.systemDefault()): Boolean {
        val zonedDateTime = date.atZone(zoneId)
        val dayOfWeek = zonedDateTime.dayOfWeek
        
        if (dayOfWeek !in WorkSchedule.WORK_DAYS) {
            return false // Not a work day, so cannot be absent
        }

        val targetDate = zonedDateTime.toLocalDate()
        val hasEntry = attendances.any { 
            it.type == AttendanceType.CHECK_IN && 
            Instant.ofEpochMilli(it.deviceTimeMillis).atZone(zoneId).toLocalDate() == targetDate 
        }

        return !hasEntry
    }
}
