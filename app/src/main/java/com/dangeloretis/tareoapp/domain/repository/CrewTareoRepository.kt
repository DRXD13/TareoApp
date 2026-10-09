package com.dangeloretis.tareoapp.domain.repository

import com.dangeloretis.tareoapp.data.local.entity.CrewTareoEntity
import kotlinx.coroutines.flow.Flow

interface CrewTareoRepository {
    suspend fun saveTareo(tareo: CrewTareoEntity)
    fun getTareosForTareador(tareadorId: String): Flow<List<CrewTareoEntity>>
    suspend fun hasWorkerRegisteredOnDay(dni: String, laborId: String, loteId: String, timestamp: Long): Boolean
}
