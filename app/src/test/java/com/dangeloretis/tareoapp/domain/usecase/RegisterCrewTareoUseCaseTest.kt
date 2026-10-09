package com.dangeloretis.tareoapp.domain.usecase

import android.os.SystemClock
import com.dangeloretis.tareoapp.data.local.entity.CrewTareoEntity
import com.dangeloretis.tareoapp.domain.model.Worker
import com.dangeloretis.tareoapp.domain.repository.CrewTareoRepository
import com.dangeloretis.tareoapp.domain.repository.WorkerDirectoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.MockedStatic
import org.mockito.Mockito.mockStatic
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class RegisterCrewTareoUseCaseTest {

    private lateinit var useCase: RegisterCrewTareoUseCase
    private lateinit var systemClockMock: MockedStatic<SystemClock>

    private val fakeWorkerDirectoryRepository = object : WorkerDirectoryRepository {
        override suspend fun getWorkerByDni(dni: String): Worker? {
            return if (dni == "11111111") Worker("11111111", "Juan Perez") else null
        }
    }

    private val fakeCrewTareoRepository = object : CrewTareoRepository {
        var hasRegistered = false
        override suspend fun saveTareo(tareo: CrewTareoEntity) {}
        override fun getTareosForTareador(tareadorId: String): Flow<List<CrewTareoEntity>> = flowOf(emptyList())
        override suspend fun hasWorkerRegisteredOnDay(
            dni: String,
            laborId: String,
            loteId: String,
            timestamp: Long
        ): Boolean {
            return hasRegistered
        }
    }

    @Before
    fun setup() {
        systemClockMock = mockStatic(SystemClock::class.java)
        systemClockMock.`when`<Long> { SystemClock.elapsedRealtime() }.thenReturn(1000L)
        useCase = RegisterCrewTareoUseCase(fakeWorkerDirectoryRepository, fakeCrewTareoRepository)
    }

    @After
    fun tearDown() {
        systemClockMock.close()
    }

    @Test
    fun `reject when labor is blank`() = runTest {
        val result = useCase("tareador1", "11111111", "", "lote1", "DNI")
        assertTrue(result.isFailure)
        assertEquals("Labor y lote son obligatorios", result.exceptionOrNull()?.message)
    }

    @Test
    fun `reject when lote is blank`() = runTest {
        val result = useCase("tareador1", "11111111", "labor1", "", "DNI")
        assertTrue(result.isFailure)
        assertEquals("Labor y lote son obligatorios", result.exceptionOrNull()?.message)
    }

    @Test
    fun `reject when dni is not 8 digits`() = runTest {
        val result = useCase("tareador1", "1111111", "labor1", "lote1", "DNI")
        assertTrue(result.isFailure)
        assertEquals("El DNI debe tener 8 dígitos", result.exceptionOrNull()?.message)
    }

    @Test
    fun `reject when worker not found in directory`() = runTest {
        val result = useCase("tareador1", "99999999", "labor1", "lote1", "DNI")
        assertTrue(result.isFailure)
        assertEquals("Trabajador no encontrado en el directorio", result.exceptionOrNull()?.message)
    }

    @Test
    fun `reject when worker already registered on the same day for labor and lote`() = runTest {
        fakeCrewTareoRepository.hasRegistered = true
        val result = useCase("tareador1", "11111111", "labor1", "lote1", "DNI")
        assertTrue(result.isFailure)
        assertEquals("El trabajador ya fue registrado hoy en esta labor y lote", result.exceptionOrNull()?.message)
    }

    @Test
    fun `success when all rules pass`() = runTest {
        fakeCrewTareoRepository.hasRegistered = false
        val result = useCase("tareador1", "11111111", "labor1", "lote1", "DNI")
        assertTrue(result.isSuccess)
        val entity = result.getOrNull()
        assertEquals("11111111", entity?.workerDni)
        assertEquals("Juan Perez", entity?.workerName)
    }
}
