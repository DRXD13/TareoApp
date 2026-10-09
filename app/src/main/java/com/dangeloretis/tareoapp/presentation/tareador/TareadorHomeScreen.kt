package com.dangeloretis.tareoapp.presentation.tareador

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.dangeloretis.tareoapp.R
import com.dangeloretis.tareoapp.data.local.entity.SyncStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TareadorHomeScreen(
    viewModel: TareadorViewModel = hiltViewModel(),
    onScanQrClick: () -> Unit,
    onLogout: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val labores by viewModel.labores.collectAsState()
    val lotes by viewModel.lotes.collectAsState()

    val context = LocalContext.current
    var laborExpanded by remember { mutableStateOf(false) }
    var loteExpanded by remember { mutableStateOf(false) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onScanQrClick()
        }
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            // Un Snackbar normal sería mejor, pero por simplicidad
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tareo de Cuadrillas") },
                actions = {
                    TextButton(onClick = { 
                        viewModel.logout()
                        onLogout()
                    }) {
                        Text("Cerrar sesión")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            // Selectores
            ExposedDropdownMenuBox(
                expanded = laborExpanded,
                onExpandedChange = { laborExpanded = it }
            ) {
                OutlinedTextField(
                    value = uiState.selectedLabor?.name ?: "Seleccionar Labor",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = laborExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = laborExpanded,
                    onDismissRequest = { laborExpanded = false }
                ) {
                    labores.forEach { labor ->
                        DropdownMenuItem(
                            text = { Text(labor.name) },
                            onClick = {
                                viewModel.onLaborSelected(labor)
                                laborExpanded = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = loteExpanded,
                onExpandedChange = { loteExpanded = it }
            ) {
                OutlinedTextField(
                    value = uiState.selectedLote?.name ?: "Seleccionar Lote",
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = loteExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = loteExpanded,
                    onDismissRequest = { loteExpanded = false }
                ) {
                    lotes.forEach { lote ->
                        DropdownMenuItem(
                            text = { Text(lote.name) },
                            onClick = {
                                viewModel.onLoteSelected(lote)
                                loteExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Ingreso manual o QR
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = uiState.dniInput,
                    onValueChange = { if (it.length <= 8) viewModel.onDniChanged(it) },
                    label = { Text("DNI") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { viewModel.registerTareo("DNI") },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Registrar")
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = {
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        onScanQrClick()
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Escanear QR")
            }

            if (uiState.message != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(uiState.message!!, color = MaterialTheme.colorScheme.primary)
                LaunchedEffect(uiState.message) {
                    kotlinx.coroutines.delay(3000)
                    viewModel.clearMessage()
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val workersCount = uiState.tareos.size
            Text(
                text = pluralStringResource(id = R.plurals.trabajadores_registrados, count = workersCount, workersCount),
                style = MaterialTheme.typography.titleMedium
            )

            LazyColumn {
                items(uiState.tareos) { tareo ->
                    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Row(modifier = Modifier.padding(16.dp)) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(tareo.workerName, style = MaterialTheme.typography.bodyLarge)
                                Text("DNI: ${tareo.workerDni}", style = MaterialTheme.typography.bodyMedium)
                                val laborName = labores.find { it.id == tareo.laborId }?.name ?: ""
                                val loteName = lotes.find { it.id == tareo.loteId }?.name ?: ""
                                Text("$laborName - $loteName", style = MaterialTheme.typography.bodySmall)
                                val timeStr = SimpleDateFormat("HH:mm:ss", LocalConfiguration.current.locales[0]).format(Date(tareo.registeredAtMillis))
                                Text("Hora: $timeStr | Método: ${tareo.method}", style = MaterialTheme.typography.bodySmall)
                            }
                            when (tareo.syncStatus) {
                                SyncStatus.SYNCED -> Icon(Icons.Default.CheckCircle, "Sincronizado", tint = Color(0xFF4CAF50))
                                SyncStatus.PENDING -> Icon(Icons.Default.Schedule, "Pendiente", tint = Color(0xFFFFC107))
                                SyncStatus.FAILED -> Icon(Icons.Default.Error, "Error", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }
}
