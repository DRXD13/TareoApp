package com.dangeloretis.tareoapp.data.repository

import com.dangeloretis.tareoapp.domain.model.Worker
import com.dangeloretis.tareoapp.domain.repository.WorkerDirectoryRepository
import javax.inject.Inject

class DemoWorkerDirectoryRepository @Inject constructor() : WorkerDirectoryRepository {
    private val workers = listOf(
        Worker("11111111", "Juan Perez"),
        Worker("12121212", "Maria Gomez"),
        Worker("13131313", "Carlos Lopez"),
        Worker("44444444", "Ana Martinez"),
        Worker("55555555", "Luis Rodriguez"),
        Worker("66666666", "Elena Sanchez"),
        Worker("77777777", "Pedro Ramirez"),
        Worker("88888888", "Sofia Torres"),
        Worker("99999999", "Jorge Diaz"),
        Worker("10101010", "Lucia Morales")
    )

    override suspend fun getWorkerByDni(dni: String): Worker? {
        return workers.find { it.dni == dni }
    }
}
