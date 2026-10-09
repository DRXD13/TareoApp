package com.dangeloretis.tareoapp.domain.usecase

import android.os.SystemClock
import com.dangeloretis.tareoapp.data.local.entity.CrewTareoEntity
import com.dangeloretis.tareoapp.data.local.entity.SyncStatus
import com.dangeloretis.tareoapp.domain.repository.CrewTareoRepository
import com.dangeloretis.tareoapp.domain.repository.WorkerDirectoryRepository
import java.util.UUID
import javax.inject.Inject

class RegisterCrewTareoUseCase @Inject constructor(
    private val workerDirectoryRepository: WorkerDirectoryRepository,
    private val crewTareoRepository: CrewTareoRepository
) {
    suspend operator fun invoke(
        tareadorId: String,
        workerDni: String,
        laborId: String,
        loteId: String,
        method: String // "DNI" or "QR"
    ): Result<CrewTareoEntity> {
        if (laborId.isBlank() || loteId.isBlank()) {
            return Result.failure(Exception("Labor y lote son obligatorios"))
        }
        
        if (workerDni.length != 8 || !workerDni.all { it.isDigit() }) {
            return Result.failure(Exception("El DNI debe tener 8 dígitos"))
        }

        val worker = workerDirectoryRepository.getWorkerByDni(workerDni)
            ?: return Result.failure(Exception("Trabajador no encontrado en el directorio"))

        val timestamp = System.currentTimeMillis()
        val hasRegistered = crewTareoRepository.hasWorkerRegisteredOnDay(workerDni, laborId, loteId, timestamp)
        
        if (hasRegistered) {
            return Result.failure(Exception("El trabajador ya fue registrado hoy en esta labor y lote"))
        }

        val entity = CrewTareoEntity(
            id = UUID.randomUUID().toString(),
            tareadorId = tareadorId,
            workerDni = workerDni,
            workerName = worker.fullName,
            laborId = laborId,
            loteId = loteId,
            registeredAtMillis = timestamp,
            elapsedRealtimeMillis = SystemClock.elapsedRealtime(),
            method = method,
            syncStatus = SyncStatus.PENDING
        )
        
        crewTareoRepository.saveTareo(entity)
        return Result.success(entity)
    }
}
