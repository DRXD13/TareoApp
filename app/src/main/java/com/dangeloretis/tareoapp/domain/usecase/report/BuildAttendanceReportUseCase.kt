package com.dangeloretis.tareoapp.domain.usecase.report

import com.dangeloretis.tareoapp.domain.model.Attendance
import com.dangeloretis.tareoapp.domain.model.AttendanceType
import com.dangeloretis.tareoapp.domain.model.WorkSchedule
import com.dangeloretis.tareoapp.domain.model.Worker
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import javax.inject.Inject

data class DailyReport(
    val date: Instant,
    val firstEntry: Attendance?,
    val lastExit: Attendance?,
    val hoursWorked: Double,
    val minutesWorked: Long,
    val state: DailyState,
    val latenessMinutes: Long
)

enum class DailyState {
    PUNTUAL,
    TARDANZA,
    FALTA,
    JORNADA_INCOMPLETA,
    PENDIENTE,
    NO_LABORABLE
}

data class WorkerReport(
    val worker: Worker,
    val daysAttended: Int,
    val absences: Int,
    val latenesses: Int,
    val totalLatenessMinutes: Long,
    val totalHoursWorked: Double,
    val totalMinutesWorked: Long,
    val dailyReports: List<DailyReport>
)

class BuildAttendanceReportUseCase @Inject constructor(
    private val calculateWorkedHoursUseCase: CalculateWorkedHoursUseCase,
    private val detectLatenessUseCase: DetectLatenessUseCase,
    private val clock: Clock
) {
    operator fun invoke(
        workers: List<Worker>,
        attendances: List<Attendance>,
        startDate: Instant,
        endDate: Instant,
        zoneId: ZoneId = ZoneId.systemDefault()
    ): List<WorkerReport> {
        val attendancesByUser = attendances.groupBy { it.userId }
        val startLocal = startDate.atZone(zoneId).toLocalDate()
        val endLocal = endDate.atZone(zoneId).toLocalDate()
        val nowZDT = ZonedDateTime.now(clock).withZoneSameInstant(zoneId)

        val dates = mutableListOf<Instant>()
        var current = startLocal
        while (!current.isAfter(endLocal)) {
            dates.add(current.atStartOfDay(zoneId).toInstant())
            current = current.plusDays(1)
        }

        return workers.map { worker ->
            val workerAttendances = attendancesByUser[worker.dni] ?: emptyList()
            var daysAttended = 0
            var absences = 0
            var latenesses = 0
            var totalLatenessMinutes = 0L
            var totalHoursWorked = 0.0
            var totalMinutesWorked = 0L

            val dailyReports = dates.mapNotNull { date ->
                val dateZDT = date.atZone(zoneId)
                val isWorkDay = dateZDT.dayOfWeek in WorkSchedule.WORK_DAYS
                
                val dayAttendances = workerAttendances.filter {
                    Instant.ofEpochMilli(it.deviceTimeMillis).atZone(zoneId).toLocalDate() == dateZDT.toLocalDate()
                }

                val hasEntry = dayAttendances.any { it.type == AttendanceType.CHECK_IN }
                
                val hoursResult = calculateWorkedHoursUseCase(workerAttendances, date, zoneId)
                val latenessMinutes = detectLatenessUseCase(workerAttendances, date, zoneId)

                val state = when {
                    !isWorkDay -> {
                        if (hasEntry) {
                            if (hoursResult.isIncomplete) DailyState.JORNADA_INCOMPLETA else DailyState.PUNTUAL
                        } else {
                            DailyState.NO_LABORABLE
                        }
                    }
                    dateZDT.toLocalDate().isAfter(nowZDT.toLocalDate()) -> DailyState.PENDIENTE
                    dateZDT.toLocalDate() == nowZDT.toLocalDate() && !hasEntry -> {
                        val expectedTime = WorkSchedule.ENTRY_TIME.plusMinutes(WorkSchedule.TOLERANCE_MINUTES)
                        if (nowZDT.toLocalTime().isBefore(expectedTime)) {
                            DailyState.PENDIENTE
                        } else {
                            DailyState.FALTA
                        }
                    }
                    !hasEntry -> DailyState.FALTA
                    hoursResult.isIncomplete -> DailyState.JORNADA_INCOMPLETA
                    latenessMinutes > 0 -> DailyState.TARDANZA
                    else -> DailyState.PUNTUAL
                }

                if (state == DailyState.FALTA) {
                    absences++
                } else if (hasEntry) {
                    daysAttended++
                    totalHoursWorked += hoursResult.hours
                    totalMinutesWorked += hoursResult.minutes
                }

                if (state != DailyState.PENDIENTE && state != DailyState.NO_LABORABLE || hasEntry) {
                    if (latenessMinutes > 0) {
                        latenesses++
                        totalLatenessMinutes += latenessMinutes
                    }
    
                    val firstEntry = dayAttendances.filter { it.type == AttendanceType.CHECK_IN }.minByOrNull { it.deviceTimeMillis }
                    val lastExit = dayAttendances.filter { it.type == AttendanceType.CHECK_OUT }.maxByOrNull { it.deviceTimeMillis }
    
                    DailyReport(
                        date = date,
                        firstEntry = firstEntry,
                        lastExit = lastExit,
                        hoursWorked = hoursResult.hours,
                        minutesWorked = hoursResult.minutes,
                        state = state,
                        latenessMinutes = latenessMinutes
                    )
                } else {
                    null
                }
            }.sortedByDescending { it.date }

            WorkerReport(
                worker = worker,
                daysAttended = daysAttended,
                absences = absences,
                latenesses = latenesses,
                totalLatenessMinutes = totalLatenessMinutes,
                totalHoursWorked = totalHoursWorked,
                totalMinutesWorked = totalMinutesWorked,
                dailyReports = dailyReports
            )
        }
    }
}
