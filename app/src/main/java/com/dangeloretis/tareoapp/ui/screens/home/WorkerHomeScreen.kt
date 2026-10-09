package com.dangeloretis.tareoapp.ui.screens.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.provider.Settings
import android.app.Activity
import androidx.core.app.ActivityCompat
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material3.Icon
import com.dangeloretis.tareoapp.data.local.entity.SyncStatus
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.dangeloretis.tareoapp.R
import com.dangeloretis.tareoapp.domain.model.AttendanceType
import com.dangeloretis.tareoapp.domain.model.UserRole
import com.dangeloretis.tareoapp.domain.usecase.ValidationResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun WorkerHomeScreen(
    onLogout: () -> Unit,
    viewModel: WorkerViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val attendances by viewModel.attendances.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    if (state.user == null) return

    val timeRemaining by viewModel.timeRemainingSeconds.collectAsState()

    var permissionGranted by remember { mutableStateOf(false) }
    var permanentlyDenied by remember { mutableStateOf(false) }
    var hasRequestedPermission by rememberSaveable { mutableStateOf(false) }
    var gpsEnabled by remember { mutableStateOf(isGpsEnabled(context)) }
    val activity = context as? Activity

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasRequestedPermission = true
        val fineLocation = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocation = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        permissionGranted = fineLocation || coarseLocation
        if (!permissionGranted) {
            val shouldShowRationale = activity?.let { 
                ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.ACCESS_FINE_LOCATION) ||
                ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.ACCESS_COARSE_LOCATION)
            } ?: false
            
            permanentlyDenied = !shouldShowRationale
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                gpsEnabled = isGpsEnabled(context)
                if (permissionGranted && gpsEnabled) {
                    viewModel.startLocationUpdates()
                }
            } else if (event == Lifecycle.Event.ON_STOP) {
                viewModel.stopLocationUpdates()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            viewModel.stopLocationUpdates()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasRequestedPermission) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(permissionGranted, gpsEnabled) {
        if (permissionGranted && gpsEnabled) {
            viewModel.startLocationUpdates()
        } else {
            viewModel.stopLocationUpdates()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.hello_user, state.user!!.fullName),
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = stringResource(R.string.role_label, stringResource(R.string.role_worker)),
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (!permissionGranted) {
            PermissionDeniedContent(
                permanentlyDenied = permanentlyDenied,
                onRequestPermission = {
                    permissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                },
                onOpenSettings = {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }
            )
        } else if (!gpsEnabled) {
            Text(
                text = stringResource(R.string.gps_disabled),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            // Location Content
            state.site?.let { site ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = stringResource(R.string.site_label, site.name), style = MaterialTheme.typography.titleMedium)
                        
                        state.distance?.let { dist ->
                            val distStr = if (dist < 1000f) {
                                stringResource(R.string.distance_format_m, dist)
                            } else {
                                stringResource(R.string.distance_format_km, dist / 1000f)
                            }
                            Text(text = stringResource(R.string.distance_label, distStr), style = MaterialTheme.typography.bodyLarge)
                            
                            val isInside = dist <= site.radiusMeters
                            val statusColor = if (isInside) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
                            val statusText = if (isInside) stringResource(R.string.inside_zone) else stringResource(R.string.outside_zone)
                            
                            Box(
                                modifier = Modifier
                                    .padding(vertical = 8.dp)
                                    .background(statusColor, shape = MaterialTheme.shapes.small)
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            ) {
                                Text(text = statusText, color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (timeRemaining > 0) {
                            Text(
                                text = stringResource(R.string.wait_time_msg, timeRemaining),
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        } else if (state.errorMessage != null) {
                            Text(
                                text = state.errorMessage!!,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val isValid = state.validationResult is ValidationResult.Valid
                val nextActionType = if (attendances.firstOrNull()?.type == AttendanceType.CHECK_IN) AttendanceType.CHECK_OUT else AttendanceType.CHECK_IN
                val btnText = if (nextActionType == AttendanceType.CHECK_IN) stringResource(R.string.check_in_btn) else stringResource(R.string.check_out_btn)

                Button(
                    onClick = { viewModel.registerAttendance() },
                    enabled = isValid && timeRemaining == 0 && !state.isRegistering,
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                ) {
                    if (state.isRegistering) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(btnText, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = stringResource(R.string.attendances_title), style = MaterialTheme.typography.titleMedium)
        
        val pendingCount = attendances.count { it.syncStatus == SyncStatus.PENDING || it.syncStatus == SyncStatus.FAILED }
        if (pendingCount > 0) {
            Text(
                text = stringResource(R.string.pending_syncs_msg, pendingCount),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
        
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        
        LazyColumn(modifier = Modifier.weight(1f).fillMaxWidth()) {
            items(attendances) { attendance ->
                val typeStr = if (attendance.type == AttendanceType.CHECK_IN) "Entrada" else "Salida"
                val timeStr = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(attendance.deviceTimeMillis))
                
                val syncIcon = when (attendance.syncStatus) {
                    SyncStatus.PENDING -> "⏳"
                    SyncStatus.SYNCED -> "✅"
                    SyncStatus.FAILED -> "❌"
                }
                val syncColor = when (attendance.syncStatus) {
                    SyncStatus.PENDING -> Color.Gray
                    SyncStatus.SYNCED -> Color(0xFF4CAF50)
                    SyncStatus.FAILED -> MaterialTheme.colorScheme.error
                }
                val syncText = when (attendance.syncStatus) {
                    SyncStatus.PENDING -> stringResource(R.string.status_pending)
                    SyncStatus.SYNCED -> stringResource(R.string.status_synced)
                    SyncStatus.FAILED -> stringResource(R.string.status_error)
                }

                ListItem(
                    headlineContent = { Text(typeStr) },
                    trailingContent = { 
                        Column(horizontalAlignment = Alignment.End) {
                            Text(timeStr)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = syncIcon,
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                                Text(
                                    text = syncText,
                                    color = syncColor,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    },
                    supportingContent = { Text("Precisión: ${attendance.accuracyMeters}m") }
                )
            }
        }

        Button(onClick = {
            viewModel.logout()
            onLogout()
        }) {
            Text(stringResource(R.string.logout_button))
        }
    }
}

@Composable
fun PermissionDeniedContent(
    permanentlyDenied: Boolean,
    onRequestPermission: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
        Text(
            text = if (permanentlyDenied) stringResource(R.string.permission_denied_permanently) else stringResource(R.string.permission_rationale),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = if (permanentlyDenied) onOpenSettings else onRequestPermission) {
            Text(if (permanentlyDenied) stringResource(R.string.open_settings_btn) else stringResource(R.string.grant_permission_btn))
        }
    }
}

private fun isGpsEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}
