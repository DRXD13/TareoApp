package com.dangeloretis.tareoapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dangeloretis.tareoapp.data.local.entity.CrewTareoEntity
import com.dangeloretis.tareoapp.data.local.entity.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
@JvmSuppressWildcards
interface CrewTareoDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tareo: CrewTareoEntity): Long

    @Query("SELECT * FROM crew_tareos WHERE tareadorId = :tareadorId ORDER BY registeredAtMillis DESC")
    fun getTareosForTareador(tareadorId: String): Flow<List<CrewTareoEntity>>

    @Query("SELECT * FROM crew_tareos WHERE workerDni = :dni AND laborId = :laborId AND loteId = :loteId AND registeredAtMillis >= :startOfDay AND registeredAtMillis < :endOfDay")
    suspend fun getTareoForWorkerOnDay(dni: String, laborId: String, loteId: String, startOfDay: Long, endOfDay: Long): CrewTareoEntity?

    @Query("SELECT * FROM crew_tareos WHERE syncStatus IN (:statuses)")
    suspend fun getTareosBySyncStatus(statuses: List<SyncStatus>): List<CrewTareoEntity>

    @Query("UPDATE crew_tareos SET syncStatus = :status, syncedAtMillis = :syncedAt WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: SyncStatus, syncedAt: Long?): Int
}
