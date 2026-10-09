package com.dangeloretis.tareoapp.data.repository

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.dangeloretis.tareoapp.data.local.dao.AttendanceDao
import com.dangeloretis.tareoapp.data.local.entity.SyncStatus
import com.dangeloretis.tareoapp.data.mapper.toDomain
import com.dangeloretis.tareoapp.data.mapper.toEntity
import com.dangeloretis.tareoapp.domain.model.Attendance
import com.dangeloretis.tareoapp.domain.repository.AttendanceRepository
import com.dangeloretis.tareoapp.worker.SyncAttendanceWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.time.Duration
import javax.inject.Inject

import java.util.Calendar

class RoomAttendanceRepository @Inject constructor(
    private val attendanceDao: AttendanceDao,
    @ApplicationContext private val context: Context
) : AttendanceRepository {
    override fun getUserAttendances(userId: String): Flow<List<Attendance>> {
        return attendanceDao.getAttendances(userId).map { list ->
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = calendar.timeInMillis
            list.filter { it.deviceTimeMillis >= startOfDay }.map { it.toDomain() }
        }
    }

    override suspend fun getLastAttendance(userId: String): Attendance? {
        return withContext(Dispatchers.IO) {
            attendanceDao.getLastAttendance(userId)?.toDomain()
        }
    }

    override suspend fun saveAttendance(attendance: Attendance) {
        withContext(Dispatchers.IO) {
            attendanceDao.insert(attendance.toEntity(syncStatusOverride = SyncStatus.PENDING))
        }
        enqueueSyncWorker()
    }

    override suspend fun getAttendancesCountForSite(siteId: String): Int {
        return withContext(Dispatchers.IO) {
            attendanceDao.getAttendancesCountForSite(siteId)
        }
    }
    
    private fun enqueueSyncWorker() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
            
        val workRequest = OneTimeWorkRequestBuilder<SyncAttendanceWorker>()
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, Duration.ofMinutes(1))
            .build()
            
        WorkManager.getInstance(context).enqueueUniqueWork(
            "SyncAttendanceWork",
            ExistingWorkPolicy.APPEND_OR_REPLACE,
            workRequest
        )
    }
}
