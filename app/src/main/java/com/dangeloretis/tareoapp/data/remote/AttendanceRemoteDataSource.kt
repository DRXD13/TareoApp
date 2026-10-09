package com.dangeloretis.tareoapp.data.remote

import com.dangeloretis.tareoapp.domain.model.Attendance

interface AttendanceRemoteDataSource {
    suspend fun upload(attendances: List<Attendance>): Result<Unit>
}
