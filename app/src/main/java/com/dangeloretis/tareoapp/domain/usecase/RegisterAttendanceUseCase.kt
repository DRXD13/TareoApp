package com.dangeloretis.tareoapp.domain.usecase

import android.os.SystemClock
import com.dangeloretis.tareoapp.domain.model.Attendance
import com.dangeloretis.tareoapp.domain.model.AttendanceType
import com.dangeloretis.tareoapp.domain.model.Site
import com.dangeloretis.tareoapp.domain.repository.AttendanceRepository
import java.util.UUID
import javax.inject.Inject

class RegisterAttendanceUseCase @Inject constructor(
    private val attendanceRepository: AttendanceRepository,
    private val validateCheckInUseCase: ValidateCheckInUseCase
) {
    suspend operator fun invoke(
        userId: String,
        site: Site,
        latitude: Double,
        longitude: Double,
        accuracy: Float,
        isMock: Boolean
    ): ValidationResult {
        val lastAttendance = attendanceRepository.getLastAttendance(userId)
        val currentElapsed = SystemClock.elapsedRealtime()

        val (validationResult, distance) = validateCheckInUseCase(
            site = site,
            latitude = latitude,
            longitude = longitude,
            accuracy = accuracy,
            isMock = isMock,
            lastAttendance = lastAttendance,
            currentElapsedRealtimeMillis = currentElapsed
        )

        if (validationResult !is ValidationResult.Valid) {
            return validationResult
        }

        val lastType = lastAttendance?.type
        val newType = if (lastType == AttendanceType.CHECK_IN) {
            AttendanceType.CHECK_OUT
        } else {
            AttendanceType.CHECK_IN
        }

        val newAttendance = Attendance(
            id = UUID.randomUUID().toString(),
            userId = userId,
            siteId = site.id,
            type = newType,
            latitude = latitude,
            longitude = longitude,
            accuracyMeters = accuracy,
            deviceTimeMillis = System.currentTimeMillis(),
            elapsedRealtimeMillis = currentElapsed,
            isMockLocation = isMock,
            distanceMeters = distance
        )
        
        attendanceRepository.saveAttendance(newAttendance)
        return ValidationResult.Valid
    }
}
