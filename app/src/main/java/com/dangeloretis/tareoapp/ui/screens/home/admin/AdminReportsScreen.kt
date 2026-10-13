package com.dangeloretis.tareoapp.ui.screens.home.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dangeloretis.tareoapp.R
import com.dangeloretis.tareoapp.domain.usecase.report.WorkerReport
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.dangeloretis.tareoapp.util.formatHoursMinutes

import androidx.compose.ui.res.pluralStringResource

import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
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
                    report.dailyReports.forEach { daily ->
                        val dateStr = DateTimeFormatter.ISO_LOCAL_DATE.withZone(ZoneId.systemDefault()).format(daily.date)
                        val formatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())
                        val entryStr = daily.firstEntry?.let { formatter.format(java.time.Instant.ofEpochMilli(it.deviceTimeMillis)) } ?: "--:--"
                        val exitStr = daily.lastExit?.let { formatter.format(java.time.Instant.ofEpochMilli(it.deviceTimeMillis)) } ?: "--:--"
                        
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(dateStr, fontWeight = FontWeight.Medium)
                            Text(daily.state.name, color = if (daily.state.name == "PUNTUAL") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                        }
                        Text("Entrada: $entryStr - Salida: $exitStr")
                        Text("Horas: ${daily.minutesWorked.formatHoursMinutes()}")
                    }
                }
            }
        }
    }
}
