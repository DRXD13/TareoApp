package com.dangeloretis.tareoapp.data.repository

import com.dangeloretis.tareoapp.data.local.dao.CrewTareoDao
import com.dangeloretis.tareoapp.data.local.entity.CrewTareoEntity
import com.dangeloretis.tareoapp.domain.repository.CrewTareoRepository
import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.dangeloretis.tareoapp.worker.SyncAttendanceWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Duration
import java.util.Calendar
import javax.inject.Inject

class RoomCrewTareoRepository @Inject constructor(
    private val crewTareoDao: CrewTareoDao,
    @ApplicationContext private val context: Context
) : CrewTareoRepository {

    override suspend fun saveTareo(tareo: CrewTareoEntity) {
        withContext(Dispatchers.IO) {
            crewTareoDao.insert(tareo)
        }
        enqueueSyncWorker()
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

    override fun getTareosForTareador(tareadorId: String): Flow<List<CrewTareoEntity>> {
        return crewTareoDao.getTareosForTareador(tareadorId)
    }

    override suspend fun hasWorkerRegisteredOnDay(
        dni: String,
        laborId: String,
        loteId: String,
        timestamp: Long
    ): Boolean {
        return withContext(Dispatchers.IO) {
            val cal = Calendar.getInstance().apply { timeInMillis = timestamp }
            cal.set(Calendar.HOUR_OF_DAY, 0)
            cal.set(Calendar.MINUTE, 0)
            cal.set(Calendar.SECOND, 0)
            cal.set(Calendar.MILLISECOND, 0)
            val startOfDay = cal.timeInMillis

            cal.add(Calendar.DAY_OF_YEAR, 1)
            val endOfDay = cal.timeInMillis

            val existingTareo = crewTareoDao.getTareoForWorkerOnDay(dni, laborId, loteId, startOfDay, endOfDay)
            existingTareo != null
        }
    }
}
