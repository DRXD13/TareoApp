package com.dangeloretis.tareoapp.ui.screens.home.admin

import android.Manifest
import android.content.Context
import android.location.LocationManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dangeloretis.tareoapp.R
import com.dangeloretis.tareoapp.domain.model.Site
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

@Composable
fun AdminSitesScreen(
    modifier: Modifier = Modifier,
    viewModel: AdminSitesViewModel = hiltViewModel()
) {
    val sites by viewModel.sites.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var selectedSite by remember { mutableStateOf<Site?>(null) }
    var siteToDelete by remember { mutableStateOf<Site?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(sites, key = { it.id }) { site ->
                SiteItem(
                    site = site,
                    onClick = {
                        selectedSite = site
                        showDialog = true
                    },
                    onToggleActive = { viewModel.toggleSiteActiveStatus(site) },
                    onDeleteClick = { siteToDelete = site }
                )
            }
        }

        FloatingActionButton(
            onClick = {
                selectedSite = null
                showDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_site))
        }

        if (siteToDelete != null) {
            AlertDialog(
                onDismissRequest = { siteToDelete = null },
                title = { Text(stringResource(R.string.delete_site_confirm_title)) },
                text = { Text(stringResource(R.string.delete_site_confirm_msg, siteToDelete!!.name)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val id = siteToDelete!!.id
                            siteToDelete = null
                            viewModel.deleteSite(id)
                        }
                    ) {
                        Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { siteToDelete = null }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }

        if (errorMessage != null) {
            AlertDialog(
                onDismissRequest = { viewModel.clearError() },
                title = { Text(stringResource(R.string.error)) },
                text = { Text(errorMessage!!) },
                confirmButton = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text(stringResource(R.string.ok))
                    }
                }
            )
        }

        if (showDialog) {
            SiteDialog(
                site = selectedSite,
                onDismiss = { showDialog = false },
                onSave = { newSite ->
                    viewModel.saveSite(newSite)
                    showDialog = false
                },
                onDelete = { site ->
                    showDialog = false
                    siteToDelete = site
                }
            )
        }
    }
}

@Composable
fun SiteItem(
    site: Site,
    onClick: () -> Unit,
    onToggleActive: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = site.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${site.latitude}, ${site.longitude}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.radius_format, site.radiusMeters),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete_site),
                    tint = MaterialTheme.colorScheme.error
                )
            }
            Switch(
                checked = site.isActive,
                onCheckedChange = { onToggleActive() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SiteDialog(
    site: Site?,
    onDismiss: () -> Unit,
    onSave: (Site) -> Unit,
    onDelete: (Site) -> Unit
) {
    var name by remember { mutableStateOf(site?.name ?: "") }
    var latitude by remember { mutableStateOf(site?.latitude?.toString() ?: "") }
    var longitude by remember { mutableStateOf(site?.longitude?.toString() ?: "") }
    var radius by remember { mutableStateOf(site?.radiusMeters?.toString() ?: "100.0") }
    
    val context = LocalContext.current
    
    var permissionGranted by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocation = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocation = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        permissionGranted = fineLocation || coarseLocation
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (site == null) stringResource(R.string.new_site) else stringResource(R.string.edit_site),
                    style = MaterialTheme.typography.titleLarge
                )
                if (site != null) {
                    IconButton(onClick = { onDelete(site) }) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = stringResource(R.string.delete_site),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.site_name)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = latitude,
                    onValueChange = { latitude = it },
                    label = { Text(stringResource(R.string.latitude)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = longitude,
                    onValueChange = { longitude = it },
                    label = { Text(stringResource(R.string.longitude)) },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                    val hasGps = lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
                    if (!permissionGranted) {
                        permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                    } else if (hasGps) {
                        if (androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
                            androidx.core.content.ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
                            fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                                .addOnSuccessListener { location ->
                                    if (location != null) {
                                        latitude = location.latitude.toString()
                                        longitude = location.longitude.toString()
                                    }
                                }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.use_my_location))
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = radius,
                onValueChange = { radius = it },
                label = { Text(stringResource(R.string.radius_meters)) },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    val lat = latitude.toDoubleOrNull() ?: 0.0
                    val lng = longitude.toDoubleOrNull() ?: 0.0
                    val rad = radius.toFloatOrNull() ?: 0f
                    val s = site?.copy(
                        name = name,
                        latitude = lat,
                        longitude = lng,
                        radiusMeters = rad
                    ) ?: Site(
                        id = "",
                        name = name,
                        latitude = lat,
                        longitude = lng,
                        radiusMeters = rad,
                        isActive = true
                    )
                    onSave(s)
                }) {
                    Text(stringResource(R.string.save))
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
