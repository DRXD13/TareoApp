package com.dangeloretis.tareoapp.domain.usecase

import com.dangeloretis.tareoapp.domain.model.Attendance
import com.dangeloretis.tareoapp.domain.model.Site
import javax.inject.Inject

sealed interface ValidationResult {
    data object Valid : ValidationResult
    data object MockLocationDetected : ValidationResult
    data object LowAccuracy : ValidationResult
    data object OutOfRadius : ValidationResult
    data class TooSoon(val timeRemainingSeconds: Int) : ValidationResult
}

class ValidateCheckInUseCase @Inject constructor(
    private val calculateDistanceUseCase: CalculateDistanceUseCase
) {
    companion object {
        const val MIN_MINUTES_BETWEEN_MARKS = 1
    }

    operator fun invoke(
        site: Site,
        latitude: Double,
        longitude: Double,
        accuracy: Float,
        isMock: Boolean,
        lastAttendance: Attendance?,
        currentElapsedRealtimeMillis: Long
    ): Pair<ValidationResult, Float> {
        val distance = calculateDistanceUseCase(
            site.latitude, site.longitude,
            latitude, longitude
        )

        if (lastAttendance != null) {
            val minDiffMillis = MIN_MINUTES_BETWEEN_MARKS * 60 * 1000L
            var diffMillis = currentElapsedRealtimeMillis - lastAttendance.elapsedRealtimeMillis
            
            if (lastAttendance.elapsedRealtimeMillis > currentElapsedRealtimeMillis) {
                diffMillis = System.currentTimeMillis() - lastAttendance.deviceTimeMillis
            }

            if (diffMillis in 0 until minDiffMillis) {
                val remainingSeconds = ((minDiffMillis - diffMillis) / 1000).toInt()
                return Pair(ValidationResult.TooSoon(remainingSeconds), distance)
            }
        }

        if (isMock) {
            return Pair(ValidationResult.MockLocationDetected, distance)
        }
        
        if (accuracy > 50f) {
            return Pair(ValidationResult.LowAccuracy, distance)
        }

        return if (distance <= site.radiusMeters) {
            Pair(ValidationResult.Valid, distance)
        } else {
            Pair(ValidationResult.OutOfRadius, distance)
        }
    }
}
