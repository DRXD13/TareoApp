package com.dangeloretis.tareoapp.domain.usecase

import com.dangeloretis.tareoapp.domain.model.Attendance
import com.dangeloretis.tareoapp.domain.model.AttendanceType
import com.dangeloretis.tareoapp.domain.model.Site
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidateCheckInUseCaseTest {

    private val calculateDistanceUseCase = object : CalculateDistanceUseCase() {
        override fun invoke(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Float {
            return if (lat1 == lat2 && lon1 == lon2) 10f else 150f
        }
    }
    
    private val validateCheckInUseCase = ValidateCheckInUseCase(calculateDistanceUseCase)
    private val site = Site("1", "Test Site", 37.0, -122.0, 100f)

    @Test
    fun `mock location should be rejected`() {
        val (result, _) = validateCheckInUseCase(site, 37.0, -122.0, 10f, true, null, 0L)
        assertEquals(ValidationResult.MockLocationDetected, result)
    }

    @Test
    fun `low accuracy should be rejected`() {
        val (result, _) = validateCheckInUseCase(site, 37.0, -122.0, 60f, false, null, 0L)
        assertEquals(ValidationResult.LowAccuracy, result)
    }

    @Test
    fun `outside radius should be rejected`() {
        val (result, distance) = validateCheckInUseCase(site, 38.0, -123.0, 10f, false, null, 0L)
        assertEquals(ValidationResult.OutOfRadius, result)
        assertEquals(150f, distance, 0.1f)
    }

    @Test
    fun `valid location should be accepted`() {
        val (result, distance) = validateCheckInUseCase(site, 37.0, -122.0, 10f, false, null, 0L)
        assertEquals(ValidationResult.Valid, result)
        assertEquals(10f, distance, 0.1f)
    }

    @Test
    fun `mark too soon after previous mark should be rejected`() {
        val lastAttendance = Attendance(
            id = "1", userId = "user1", siteId = "1", type = AttendanceType.CHECK_IN,
            latitude = 37.0, longitude = -122.0, accuracyMeters = 10f,
            deviceTimeMillis = 1000L, elapsedRealtimeMillis = 10000L,
            isMockLocation = false, distanceMeters = 10f
        )
        // 30 seconds passed (10000 + 30000 = 40000)
        val (result, _) = validateCheckInUseCase(site, 37.0, -122.0, 10f, false, lastAttendance, 40000L)
        assertTrue(result is ValidationResult.TooSoon)
        assertEquals(30, (result as ValidationResult.TooSoon).timeRemainingSeconds)
    }
}
