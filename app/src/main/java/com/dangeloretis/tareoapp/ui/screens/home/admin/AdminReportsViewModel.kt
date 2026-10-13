package com.dangeloretis.tareoapp.ui.screens.home.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dangeloretis.tareoapp.domain.model.Attendance
import com.dangeloretis.tareoapp.domain.model.AttendanceType
import com.dangeloretis.tareoapp.domain.repository.AttendanceRepository
import com.dangeloretis.tareoapp.domain.repository.WorkerDirectoryRepository
import com.dangeloretis.tareoapp.domain.usecase.report.BuildAttendanceReportUseCase
import com.dangeloretis.tareoapp.domain.usecase.report.WorkerReport
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.Clock
import java.time.temporal.ChronoUnit
import java.util.UUID
import javax.inject.Inject
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.dangeloretis.tareoapp.domain.repository.CrewTareoRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.flow.*
import com.dangeloretis.tareoapp.domain.repository.CatalogRepository
import com.dangeloretis.tareoapp.domain.model.Labor
import com.dangeloretis.tareoapp.domain.model.Lote
import com.dangeloretis.tareoapp.data.local.entity.SyncStatus

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class AdminReportsState(
    val selectedDateRange: DateRange = DateRange.TODAY,
    val reports: List<WorkerReport> = emptyList(),
    val isLoading: Boolean = false,
    val summaryPresent: Int = 0,
    val summaryLateness: Int = 0,
    val summaryAbsence: Int = 0,
    val summaryHours: Double = 0.0,
    val tareosPorLabor: Map<String, Int> = emptyMap()
)

enum class DateRange {
    TODAY,
    LAST_7_DAYS,
    THIS_MONTH
}

@HiltViewModel
class AdminReportsViewModel @Inject constructor(
    private val attendanceRepository: AttendanceRepository,
    private val workerDirectoryRepository: WorkerDirectoryRepository,
    private val crewTareoRepository: CrewTareoRepository,
    private val catalogRepository: CatalogRepository,
    private val buildAttendanceReportUseCase: BuildAttendanceReportUseCase,
    private val clock: Clock
) : ViewModel() {

    private val _state = MutableStateFlow(AdminReportsState())
    val state: StateFlow<AdminReportsState> = _state.asStateFlow()
    
    private val _uiEvents = MutableSharedFlow<String>()
    val uiEvents: SharedFlow<String> = _uiEvents.asSharedFlow()

    init {
        loadReport()
    }

    fun setDateRange(range: DateRange) {
        _state.update { it.copy(selectedDateRange = range) }
        loadReport()
    }

    private fun loadReport() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            val range = _state.value.selectedDateRange
            val zoneId = ZoneId.systemDefault()
            val now = ZonedDateTime.now(clock).withZoneSameInstant(zoneId)
            
            val (startDate, endDate) = when (range) {
                DateRange.TODAY -> Pair(
                    now.toLocalDate().atStartOfDay(zoneId).toInstant(),
                    now.toLocalDate().atTime(23, 59, 59).atZone(zoneId).toInstant()
                )
                DateRange.LAST_7_DAYS -> Pair(
                    now.minusDays(6).toLocalDate().atStartOfDay(zoneId).toInstant(),
                    now.toLocalDate().atTime(23, 59, 59).atZone(zoneId).toInstant()
                )
                DateRange.THIS_MONTH -> Pair(
                    now.withDayOfMonth(1).toLocalDate().atStartOfDay(zoneId).toInstant(),
                    now.toLocalDate().atTime(23, 59, 59).atZone(zoneId).toInstant()
                )
            }

            // In a real app we'd fetch all workers and attendances from DB
            // Here we assume we fetch all workers by some API. DemoWorkerDirectoryRepository doesn't have getAll.
            // Let's add a hack: fetch hardcoded list if repo doesn't support it, but I'll add getAllWorkers to WorkerDirectoryRepository.
            
            val workers = getWorkers()
            val attendances = attendanceRepository.getAllAttendances()
            
            val reports = buildAttendanceReportUseCase(
                workers,
                attendances,
                startDate,
                endDate,
                zoneId
            )
            
            val allTareos = crewTareoRepository.getAllTareos()
            val filteredTareos = allTareos.filter { 
                it.registeredAtMillis >= startDate.toEpochMilli() && 
                it.registeredAtMillis <= endDate.toEpochMilli() 
            }
            
            // Get labor and lote catalogs
            val labores = catalogRepository.getLabores().first()
            val lotes = catalogRepository.getLotes().first()
            
            val tareosMap = filteredTareos.groupBy { tareo ->
                val laborName = labores.find { it.id == tareo.laborId }?.name ?: tareo.laborId
                val loteName = lotes.find { it.id == tareo.loteId }?.name ?: tareo.loteId
                "$laborName \u00B7 $loteName"
            }.mapValues { it.value.size }
            
            val summaryPresent = reports.sumOf { it.daysAttended }
            val summaryLateness = reports.sumOf { it.latenesses }
            val summaryAbsence = reports.sumOf { it.absences }
            val summaryHours = reports.sumOf { it.totalHoursWorked }
            
            _state.update { 
                it.copy(
                    reports = reports,
                    isLoading = false,
                    summaryPresent = summaryPresent,
                    summaryLateness = summaryLateness,
                    summaryAbsence = summaryAbsence,
                    summaryHours = summaryHours,
                    tareosPorLabor = tareosMap
                ) 
            }
        }
    }
    
    private suspend fun getWorkers(): List<com.dangeloretis.tareoapp.domain.model.Worker> {
        // Need to add getAllWorkers to interface or use the hardcoded ones here for now
        // Let's assume the interface is updated.
        return workerDirectoryRepository.getAllWorkers()
    }

    fun loadMockData() {
        viewModelScope.launch {
            val workers = getWorkers()
            val zoneId = ZoneId.systemDefault()
            val now = ZonedDateTime.now(clock).withZoneSameInstant(zoneId)
            val nowMillis = now.toInstant().toEpochMilli()
            
            val mockAttendances = mutableListOf<Attendance>()
            
            workers.forEachIndexed { index, worker ->
                for (i in 0..6) {
                    val date = now.minusDays(i.toLong())
                    if (date.dayOfWeek !in com.dangeloretis.tareoapp.domain.model.WorkSchedule.WORK_DAYS) continue
                    
                    val stateType = (index + i) % 4
                    if (stateType == 0) continue // Absent
                    
                    val entryTimeZDT = if (stateType == 1) { // Late
                        date.withHour(8).withMinute(20).withSecond(0).withNano(0)
                    } else { // Punctual or Incomplete
                        date.withHour(7).withMinute(50).withSecond(0).withNano(0)
                    }
                    val entryMillis = entryTimeZDT.toInstant().toEpochMilli()
                    
                    if (entryMillis <= nowMillis) {
                        mockAttendances.add(
                            Attendance(
                                id = "mock_${UUID.randomUUID()}",
                                userId = worker.dni,
                                siteId = "demo_site_1",
                                type = AttendanceType.CHECK_IN,
                                latitude = 0.0,
                                longitude = 0.0,
                                accuracyMeters = 0f,
                                deviceTimeMillis = entryMillis,
                                elapsedRealtimeMillis = 0L,
                                isMockLocation = false,
                                distanceMeters = 0f,
                                syncStatus = SyncStatus.SYNCED
                            )
                        )
                        
                        if (stateType != 3) { // Complete shift
                            val exitTimeZDT = date.withHour(17).withMinute(10).withSecond(0).withNano(0)
                            val exitMillis = exitTimeZDT.toInstant().toEpochMilli()
                            if (exitMillis <= nowMillis) {
                                mockAttendances.add(
                                    Attendance(
                                        id = "mock_${UUID.randomUUID()}",
                                        userId = worker.dni,
                                        siteId = "demo_site_1",
                                        type = AttendanceType.CHECK_OUT,
                                        latitude = 0.0,
                                        longitude = 0.0,
                                        accuracyMeters = 0f,
                                        deviceTimeMillis = exitMillis,
                                        elapsedRealtimeMillis = 0L,
                                        isMockLocation = false,
                                        distanceMeters = 0f,
                                        syncStatus = SyncStatus.SYNCED
                                    )
                                )
                            }
                        }
                    }
                }
            }
            
            attendanceRepository.saveAttendances(mockAttendances)
            
            _uiEvents.emit("Se cargaron ${mockAttendances.size} marcajes de ejemplo")
            loadReport()
        }
    }
    
    fun clearMockData() {
        viewModelScope.launch {
            val attendances = attendanceRepository.getAllAttendances().filter { it.id.startsWith("mock_") }
            val count = attendances.size
            attendanceRepository.deleteAttendances(attendances.map { it.id })
            
            _uiEvents.emit("Se borraron $count marcajes de ejemplo")
            loadReport()
        }
    }

    fun exportToCsv(context: Context) {
        viewModelScope.launch {
            try {
                val reports = _state.value.reports
                val cacheDir = context.cacheDir
                val file = File(cacheDir, "reporte_asistencia.csv")
                
                OutputStreamWriter(file.outputStream(), StandardCharsets.UTF_8).use { writer ->
                    // BOM
                    writer.write("\uFEFF")
                    writer.write("DNI;Trabajador;Dias Asistidos;Faltas;Tardanzas;Minutos Retraso;Horas Trabajadas;Minutos Trabajados\n")
                    reports.forEach { report ->
                        writer.write("${report.worker.dni};${report.worker.fullName};${report.daysAttended};${report.absences};${report.latenesses};${report.totalLatenessMinutes};${String.format(java.util.Locale.US, "%.2f", report.totalHoursWorked)};${report.totalMinutesWorked}\n")
                    }
                }
                
                val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                
                val chooser = Intent.createChooser(intent, "Compartir Reporte")
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(chooser)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
