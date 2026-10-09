package com.dangeloretis.tareoapp.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.dangeloretis.tareoapp.data.local.dao.AttendanceDao
import com.dangeloretis.tareoapp.data.local.entity.SyncStatus
import com.dangeloretis.tareoapp.data.mapper.toDomain
import com.dangeloretis.tareoapp.data.remote.AttendanceRemoteDataSource
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class SyncAttendanceWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val attendanceDao: AttendanceDao,
    private val remoteDataSource: AttendanceRemoteDataSource
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val pendingEntities = withContext(Dispatchers.IO) {
            attendanceDao.getAttendancesBySyncStatus(
                listOf(SyncStatus.PENDING, SyncStatus.FAILED)
            )
        }

        if (pendingEntities.isEmpty()) {
            return Result.success()
        }

        val domainAttendances = pendingEntities.map { it.toDomain() }
        val ids = pendingEntities.map { it.id }

        return try {
            val result = remoteDataSource.upload(domainAttendances)
            withContext(Dispatchers.IO) {
                if (result.isSuccess) {
                    attendanceDao.updateSyncStatus(ids, SyncStatus.SYNCED, System.currentTimeMillis())
                    Result.success()
                } else {
                    attendanceDao.updateSyncStatus(ids, SyncStatus.FAILED, null)
                    Result.retry()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.IO) {
                attendanceDao.updateSyncStatus(ids, SyncStatus.FAILED, null)
            }
            Result.retry()
        }
    }
}
