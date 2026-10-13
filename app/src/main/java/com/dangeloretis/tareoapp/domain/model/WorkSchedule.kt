package com.dangeloretis.tareoapp.domain.model

import java.time.DayOfWeek
import java.time.LocalTime

object WorkSchedule {
    val ENTRY_TIME: LocalTime = LocalTime.of(8, 0)
    val EXIT_TIME: LocalTime = LocalTime.of(17, 0)
    const val TOLERANCE_MINUTES: Long = 10L
    
    val WORK_DAYS = setOf(
        DayOfWeek.MONDAY,
        DayOfWeek.TUESDAY,
        DayOfWeek.WEDNESDAY,
        DayOfWeek.THURSDAY,
        DayOfWeek.FRIDAY,
        DayOfWeek.SATURDAY
    )
}
