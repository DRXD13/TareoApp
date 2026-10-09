package com.dangeloretis.tareoapp.domain.repository

import com.dangeloretis.tareoapp.domain.model.Worker

interface WorkerDirectoryRepository {
    suspend fun getWorkerByDni(dni: String): Worker?
}
