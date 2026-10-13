package com.dangeloretis.tareoapp.ui.screens.home.admin

import android.content.Context
import com.dangeloretis.tareoapp.data.local.entity.CrewTareoEntity
import com.dangeloretis.tareoapp.domain.model.Attendance
import com.dangeloretis.tareoapp.domain.model.Labor
import com.dangeloretis.tareoapp.domain.model.Lote
import com.dangeloretis.tareoapp.domain.model.Worker
import com.dangeloretis.tareoapp.domain.repository.AttendanceRepository
import com.dangeloretis.tareoapp.domain.repository.CatalogRepository
import com.dangeloretis.tareoapp.domain.repository.CrewTareoRepository
import com.dangeloretis.tareoapp.domain.repository.WorkerDirectoryRepository
import com.dangeloretis.tareoapp.domain.usecase.report.BuildAttendanceReportUseCase
import com.dangeloretis.tareoapp.domain.usecase.report.CalculateWorkedHoursUseCase
import com.dangeloretis.tareoapp.domain.usecase.report.DetectAbsencesUseCase
import com.dangeloretis.tareoapp.domain.usecase.report.DetectLatenessUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.lang.reflect.Proxy
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class AdminReportsViewModelTest {

    private lateinit var viewModel: AdminReportsViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val attendancesFlow = MutableStateFlow<List<Attendance>>(emptyList())

    private val mockAttendanceRepo = object : AttendanceRepository {
        override fun getUserAttendances(userId: String) = flowOf(emptyList<Attendance>())
        override suspend fun getLastAttendance(userId: String): Attendance? = null
        override suspend fun saveAttendance(attendance: Attendance) {
            attendancesFlow.value = attendancesFlow.value + attendance
        }
        override suspend fun saveAttendances(attendances: List<Attendance>) {
            attendancesFlow.value = attendancesFlow.value + attendances
        }
        override suspend fun getAttendancesCountForSite(siteId: String) = 0
        override suspend fun getAllAttendances() = attendancesFlow.value
        override suspend fun deleteAttendances(ids: List<String>) {
            attendancesFlow.value = attendancesFlow.value.filter { it.id !in ids }
        }
    }

    private val workers = List(10) { i -> Worker("1111111$i", "Worker $i") }

    private val mockWorkerRepo = object : WorkerDirectoryRepository {
        override suspend fun getWorkerByDni(dni: String) = workers.find { it.dni == dni }
        override suspend fun getAllWorkers() = workers
    }

    private val mockCrewTareoRepo = object : CrewTareoRepository {
        override suspend fun saveTareo(tareo: CrewTareoEntity) {}
        override fun getTareosForTareador(tareadorId: String) = flowOf(emptyList<CrewTareoEntity>())
        override suspend fun hasWorkerRegisteredOnDay(dni: String, laborId: String, loteId: String, timestamp: Long) = false
        override suspend fun getAllTareos() = emptyList<CrewTareoEntity>()
    }

    private val mockCatalogRepo = object : CatalogRepository {
        override fun getLabores() = flowOf(emptyList<Labor>())
        override fun getLotes() = flowOf(emptyList<Lote>())
    }

    private val defaultClock = Clock.fixed(Instant.parse("2023-10-16T12:00:00Z"), ZoneId.of("UTC"))

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        val buildUseCase = BuildAttendanceReportUseCase(
            CalculateWorkedHoursUseCase(),
            DetectLatenessUseCase(),
            defaultClock
        )

        viewModel = AdminReportsViewModel(
            attendanceRepository = mockAttendanceRepo,
            workerDirectoryRepository = mockWorkerRepo,
            crewTareoRepository = mockCrewTareoRepo,
            catalogRepository = mockCatalogRepo,
            buildAttendanceReportUseCase = buildUseCase,
            clock = defaultClock
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `loadMockData inserts attendances and report has hours for at least 5 workers`() = runTest {
        viewModel.setDateRange(DateRange.LAST_7_DAYS)
        advanceUntilIdle()

        viewModel.loadMockData()
        advanceUntilIdle()

        val state = viewModel.state.value
        val workersWithHours = state.reports.count { it.totalHoursWorked > 0.0 }
        
        assertTrue("Expected at least 5 workers with hours, but found $workersWithHours", workersWithHours >= 5)
    }

    @Test
    fun `loadMockData with clock at 05 30 generates no attendances for today`() = runTest {
        val clockAt0530 = Clock.fixed(Instant.parse("2023-10-16T05:30:00Z"), ZoneId.of("UTC"))
        val buildUseCase = BuildAttendanceReportUseCase(
            CalculateWorkedHoursUseCase(),
            DetectLatenessUseCase(),
            clockAt0530
        )
        val vm = AdminReportsViewModel(
            attendanceRepository = mockAttendanceRepo,
            workerDirectoryRepository = mockWorkerRepo,
            crewTareoRepository = mockCrewTareoRepo,
            catalogRepository = mockCatalogRepo,
            buildAttendanceReportUseCase = buildUseCase,
            clock = clockAt0530
        )
        vm.setDateRange(DateRange.TODAY)
        advanceUntilIdle()

        vm.loadMockData()
        advanceUntilIdle()

        val allAttendances = mockAttendanceRepo.getAllAttendances()
        val todayAttendances = allAttendances.filter {
            Instant.ofEpochMilli(it.deviceTimeMillis).atZone(ZoneId.of("UTC")).toLocalDate() == Instant.parse("2023-10-16T05:30:00Z").atZone(ZoneId.of("UTC")).toLocalDate()
        }
        assertTrue("Expected 0 attendances for today when clock is at 05:30, but found ${todayAttendances.size}", todayAttendances.isEmpty())
    }
}