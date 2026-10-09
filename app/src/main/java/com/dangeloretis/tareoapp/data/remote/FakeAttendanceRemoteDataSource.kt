package com.dangeloretis.tareoapp.data.remote

import com.dangeloretis.tareoapp.domain.model.Attendance
import kotlinx.coroutines.delay
import javax.inject.Inject

class FakeAttendanceRemoteDataSource @Inject constructor() : AttendanceRemoteDataSource {
    override suspend fun upload(attendances: List<Attendance>): Result<Unit> {
        delay(2000L) // Simulate network delay
        return Result.success(Unit)
    }
}
