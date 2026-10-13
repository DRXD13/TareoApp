package com.dangeloretis.tareoapp.ui.screens.home.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dangeloretis.tareoapp.R
import com.dangeloretis.tareoapp.domain.usecase.report.DailyState
import com.dangeloretis.tareoapp.domain.usecase.report.WorkerReport
import com.dangeloretis.tareoapp.util.formatHoursMinutes
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminReportsScreen(
    modifier: Modifier = Modifier,
    viewModel: AdminReportsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        viewModel.uiEvents.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    DateRange.entries.forEach { range ->
                        val rangeName = when (range) {
                            DateRange.TODAY -> stringResource(R.string.report_today)
                            DateRange.LAST_7_DAYS -> stringResource(R.string.report_last_7_days)
                            DateRange.THIS_MONTH -> stringResource(R.string.report_this_month)
                        }
                        FilterChip(
                            selected = state.selectedDateRange == range,
                            onClick = { viewModel.setDateRange(range) },
                            label = { Text(rangeName) }
                        )
                    }
                }
            }
            
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val presentesTitle = if (state.selectedDateRange == DateRange.TODAY) stringResource(R.string.report_present) else stringResource(R.string.report_attendances)
                    SummaryCard(title = presentesTitle, value = state.summaryPresent.toString(), modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(8.dp))
                    SummaryCard(title = stringResource(R.string.report_lateness), value = state.summaryLateness.toString(), modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    SummaryCard(title = stringResource(R.string.report_absence), value = state.summaryAbsence.toString(), modifier = Modifier.weight(1f))
                    Spacer(modifier = Modifier.width(8.dp))
                    
                    val totalMinutesForSummary = state.reports.sumOf { it.totalMinutesWorked }
                    SummaryCard(title = stringResource(R.string.report_hours), value = totalMinutesForSummary.formatHoursMinutes(), modifier = Modifier.weight(1f))
                }
            }

            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = { viewModel.exportToCsv(context) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.export_csv))
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.loadMockData() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.load_mocks))
                        }
                        OutlinedButton(
                            onClick = { viewModel.clearMockData() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(stringResource(R.string.clear_mocks))
                        }
                    }
                }
            }

            if (state.tareosPorLabor.isNotEmpty()) {
                item {
                    Text(stringResource(R.string.tareo_por_labor), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    state.tareosPorLabor.forEach { (key, count) ->
                        Text("$key: ${pluralStringResource(R.plurals.registros_count, count, count)}")
                    }
                }
            }

            item {
                Text(stringResource(R.string.detail_by_worker), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            items(state.reports) { report ->
                WorkerReportItem(report)
            }
        }
    }
}

@Composable
fun SummaryCard(title: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, style = MaterialTheme.typography.bodySmall)
            Text(text = value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun StateChip(state: DailyState) {
    val (labelRes, containerColor, contentColor) = when (state) {
        DailyState.PUNTUAL -> Triple(
            R.string.state_puntual,
            Color(0xFFE8F5E9),
            Color(0xFF2E7D32)
        )
        DailyState.TARDANZA -> Triple(
            R.string.state_tardanza,
            Color(0xFFFFF3E0),
            Color(0xFFE65100)
        )
        DailyState.FALTA -> Triple(
            R.string.state_falta,
            Color(0xFFFFEBEE),
            Color(0xFFC62828)
        )
        DailyState.JORNADA_INCOMPLETA -> Triple(
            R.string.state_jornada_incompleta,
            Color(0xFFEEEEEE),
            Color(0xFF616161)
        )
        DailyState.PENDIENTE -> Triple(
            R.string.state_pendiente,
            Color(0xFFF5F5F5),
            Color(0xFF757575)
        )
        DailyState.NO_LABORABLE -> Triple(
            R.string.state_no_laborable,
            Color(0xFFFAFAFA),
            Color(0xFF9E9E9E)
        )
    }

    Surface(
        color = containerColor,
        contentColor = contentColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = stringResource(labelRes),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WorkerReportItem(report: WorkerReport) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(report.worker.fullName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("DNI: ${report.worker.dni}", style = MaterialTheme.typography.bodyMedium)
                }
                Icon(
                    imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Asistencias: ${report.daysAttended} | Faltas: ${report.absences}")
            Text("Tardanzas: ${report.latenesses} (${report.totalLatenessMinutes} min) | Horas: ${report.totalMinutesWorked.formatHoursMinutes()}")

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    val spanishLocale = Locale.forLanguageTag("es-ES")
                    val datePattern = DateTimeFormatter.ofPattern("EEE dd/MM", spanishLocale).withZone(ZoneId.systemDefault())

                    report.dailyReports.forEach { daily ->
                        val rawDateStr = datePattern.format(daily.date)
                        val dateStr = rawDateStr.replaceFirstChar { if (it.isLowerCase()) it.titlecase(spanishLocale) else it.toString() }
                        
                        val formatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())
                        val entryStr = daily.firstEntry?.let { formatter.format(java.time.Instant.ofEpochMilli(it.deviceTimeMillis)) } ?: "--:--"
                        val exitStr = daily.lastExit?.let { formatter.format(java.time.Instant.ofEpochMilli(it.deviceTimeMillis)) } ?: "--:--"
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(dateStr, fontWeight = FontWeight.Medium)
                            StateChip(state = daily.state)
                        }
                        Text("Entrada: $entryStr - Salida: $exitStr")
                        Text("Horas: ${daily.minutesWorked.formatHoursMinutes()}")
                    }
                }
            }
        }
    }
}
