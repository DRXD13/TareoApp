package com.dangeloretis.tareoapp.domain.repository

import com.dangeloretis.tareoapp.domain.model.Attendance
import kotlinx.coroutines.flow.Flow

interface AttendanceRepository {
    fun getUserAttendances(userId: String): Flow<List<Attendance>>
    suspend fun getLastAttendance(userId: String): Attendance?
    suspend fun saveAttendance(attendance: Attendance)
    suspend fun saveAttendances(attendances: List<Attendance>)
    suspend fun getAttendancesCountForSite(siteId: String): Int
    suspend fun getAllAttendances(): List<Attendance>
    suspend fun deleteAttendances(ids: List<String>)
}
