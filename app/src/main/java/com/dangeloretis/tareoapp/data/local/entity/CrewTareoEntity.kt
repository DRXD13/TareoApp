package com.dangeloretis.tareoapp.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "crew_tareos")
data class CrewTareoEntity(
    @PrimaryKey val id: String,
    val tareadorId: String,
    val workerDni: String,
    val workerName: String,
    val laborId: String,
    val loteId: String,
    val registeredAtMillis: Long,
    val elapsedRealtimeMillis: Long,
    val method: String, // "DNI" o "QR"
    val syncStatus: SyncStatus,
    val syncedAtMillis: Long? = null
)
