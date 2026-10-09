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

import com.dangeloretis.tareoapp.data.local.dao.CrewTareoDao
import com.dangeloretis.tareoapp.data.remote.CrewTareoRemoteDataSource

@HiltWorker
class SyncAttendanceWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val attendanceDao: AttendanceDao,
    private val remoteDataSource: AttendanceRemoteDataSource,
    private val crewTareoDao: CrewTareoDao,
    private val crewTareoRemoteDataSource: CrewTareoRemoteDataSource
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val pendingAttendances = withContext(Dispatchers.IO) {
            attendanceDao.getAttendancesBySyncStatus(
                listOf(SyncStatus.PENDING, SyncStatus.FAILED)
            )
        }
        val pendingTareos = withContext(Dispatchers.IO) {
            crewTareoDao.getTareosBySyncStatus(
                listOf(SyncStatus.PENDING, SyncStatus.FAILED)
            )
        }

        if (pendingAttendances.isEmpty() && pendingTareos.isEmpty()) {
            return Result.success()
        }

        var allSuccess = true

        if (pendingAttendances.isNotEmpty()) {
            val domainAttendances = pendingAttendances.map { it.toDomain() }
            val ids = pendingAttendances.map { it.id }
            try {
                val result = remoteDataSource.upload(domainAttendances)
                withContext(Dispatchers.IO) {
                    if (result.isSuccess) {
                        attendanceDao.updateSyncStatus(ids, SyncStatus.SYNCED, System.currentTimeMillis())
                    } else {
                        attendanceDao.updateSyncStatus(ids, SyncStatus.FAILED, null)
                        allSuccess = false
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.IO) {
                    attendanceDao.updateSyncStatus(ids, SyncStatus.FAILED, null)
                }
                allSuccess = false
            }
        }

        if (pendingTareos.isNotEmpty()) {
            for (tareo in pendingTareos) {
                try {
                    val result = crewTareoRemoteDataSource.syncTareo(tareo)
                    withContext(Dispatchers.IO) {
                        if (result.isSuccess) {
                            crewTareoDao.updateSyncStatus(tareo.id, SyncStatus.SYNCED, System.currentTimeMillis())
                        } else {
                            crewTareoDao.updateSyncStatus(tareo.id, SyncStatus.FAILED, null)
                            allSuccess = false
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.IO) {
                        crewTareoDao.updateSyncStatus(tareo.id, SyncStatus.FAILED, null)
                    }
                    allSuccess = false
                }
            }
        }

        return if (allSuccess) Result.success() else Result.retry()
    }
}
