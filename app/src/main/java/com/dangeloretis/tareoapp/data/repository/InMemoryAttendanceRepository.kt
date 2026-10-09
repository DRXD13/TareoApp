package com.dangeloretis.tareoapp.data.repository

import com.dangeloretis.tareoapp.domain.model.Attendance
import com.dangeloretis.tareoapp.domain.repository.AttendanceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class InMemoryAttendanceRepository @Inject constructor() : AttendanceRepository {
    private val attendancesFlow = MutableStateFlow<List<Attendance>>(emptyList())

    override fun getUserAttendances(userId: String): Flow<List<Attendance>> {
        return attendancesFlow.map { list -> list.filter { it.userId == userId } }
    }

    override suspend fun getLastAttendance(userId: String): Attendance? {
        return attendancesFlow.value.filter { it.userId == userId }.maxByOrNull { it.deviceTimeMillis }
    }

    override suspend fun saveAttendance(attendance: Attendance) {
        val currentList = attendancesFlow.value
        attendancesFlow.value = currentList + attendance
    }
}
