package com.dangeloretis.tareoapp.domain.usecase

import com.dangeloretis.tareoapp.domain.model.Attendance
import com.dangeloretis.tareoapp.domain.model.AttendanceType
import com.dangeloretis.tareoapp.domain.model.Site
import com.dangeloretis.tareoapp.domain.repository.AttendanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.mockStatic
import org.mockito.junit.MockitoJUnitRunner
import android.os.SystemClock

@RunWith(MockitoJUnitRunner::class)
class RegisterAttendanceUseCaseTest {

    private val attendanceRepository = object : AttendanceRepository {
        val attendancesFlow = MutableStateFlow<List<Attendance>>(emptyList())
        override fun getUserAttendances(userId: String): Flow<List<Attendance>> {
            return attendancesFlow.map { list -> list.filter { it.userId == userId }.sortedByDescending { it.deviceTimeMillis } }
        }
        override suspend fun getLastAttendance(userId: String): Attendance? {
            return attendancesFlow.value.filter { it.userId == userId }.maxByOrNull { it.deviceTimeMillis }
        }
        override suspend fun saveAttendance(attendance: Attendance) {
            attendancesFlow.value = attendancesFlow.value + attendance
        }
    }

    private val calculateDistanceUseCase = CalculateDistanceUseCase()
    private val validateCheckInUseCase = ValidateCheckInUseCase(calculateDistanceUseCase)
    private val registerAttendanceUseCase = RegisterAttendanceUseCase(attendanceRepository, validateCheckInUseCase)

    @Test
    fun `second mark immediately after first should be rejected`() = runBlocking {
        mockStatic(SystemClock::class.java).use { mockedSystemClock ->
            mockedSystemClock.`when`<Long> { SystemClock.elapsedRealtime() }.thenReturn(1000L)
            val site = Site("1", "Test Site", 37.4220, -122.0841, 100f)
            val userId = "user1"

            val result1 = registerAttendanceUseCase(userId, site, 37.4220, -122.0841, 10f, false)
            assertEquals(ValidationResult.Valid, result1)

            val result2 = registerAttendanceUseCase(userId, site, 37.4220, -122.0841, 10f, false)
            assertTrue(result2 is ValidationResult.TooSoon)
            
            val attendances = attendanceRepository.attendancesFlow.value
            assertEquals(1, attendances.size)
        }
    }

    @Test
    fun `alternates CHECK_IN and CHECK_OUT`() = runBlocking {
        mockStatic(SystemClock::class.java).use { mockedSystemClock ->
            val site = Site("1", "Test Site", 37.4220, -122.0841, 100f)
            val userId = "user2"

            mockedSystemClock.`when`<Long> { SystemClock.elapsedRealtime() }.thenReturn(1000L)
            val result1 = registerAttendanceUseCase(userId, site, 37.4220, -122.0841, 10f, false)
            assertEquals(ValidationResult.Valid, result1)

            mockedSystemClock.`when`<Long> { SystemClock.elapsedRealtime() }.thenReturn(1000L + 61 * 1000L)
            val result2 = registerAttendanceUseCase(userId, site, 37.4220, -122.0841, 10f, false)
            assertEquals(ValidationResult.Valid, result2)

            mockedSystemClock.`when`<Long> { SystemClock.elapsedRealtime() }.thenReturn(1000L + 122 * 1000L)
            val result3 = registerAttendanceUseCase(userId, site, 37.4220, -122.0841, 10f, false)
            assertEquals(ValidationResult.Valid, result3)

            val attendances = attendanceRepository.attendancesFlow.value.sortedBy { it.elapsedRealtimeMillis }
            assertEquals(3, attendances.size)
            assertEquals(AttendanceType.CHECK_IN, attendances[0].type)
            assertEquals(AttendanceType.CHECK_OUT, attendances[1].type)
            assertEquals(AttendanceType.CHECK_IN, attendances[2].type)
        }
    }
}
