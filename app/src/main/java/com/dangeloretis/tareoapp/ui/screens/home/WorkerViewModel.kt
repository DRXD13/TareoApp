package com.dangeloretis.tareoapp.ui.screens.home

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dangeloretis.tareoapp.R
import com.dangeloretis.tareoapp.domain.model.Attendance
import com.dangeloretis.tareoapp.domain.model.Site
import com.dangeloretis.tareoapp.domain.model.User
import com.dangeloretis.tareoapp.domain.repository.AttendanceRepository
import com.dangeloretis.tareoapp.domain.repository.AuthRepository
import com.dangeloretis.tareoapp.domain.repository.SiteRepository
import com.dangeloretis.tareoapp.domain.usecase.GetClosestSiteUseCase
import com.dangeloretis.tareoapp.domain.usecase.RegisterAttendanceUseCase
import com.dangeloretis.tareoapp.domain.usecase.ValidateCheckInUseCase
import com.dangeloretis.tareoapp.domain.usecase.ValidationResult
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

data class WorkerState(
    val user: User? = null,
    val site: Site? = null,
    val currentLocation: Location? = null,
    val distance: Float? = null,
    val validationResult: ValidationResult? = null,
    val errorMessage: String? = null,
    val isRegistering: Boolean = false
)

@HiltViewModel
class WorkerViewModel @Inject constructor(
    private val application: Application,
    private val authRepository: AuthRepository,
    private val siteRepository: SiteRepository,
    private val attendanceRepository: AttendanceRepository,
    private val validateCheckInUseCase: ValidateCheckInUseCase,
    private val registerAttendanceUseCase: RegisterAttendanceUseCase,
    private val getClosestSiteUseCase: GetClosestSiteUseCase
) : ViewModel() {

    private val fusedLocationClient: FusedLocationProviderClient = 
        LocationServices.getFusedLocationProviderClient(application)
        
    private var locationCallback: LocationCallback? = null
    private val mutex = Mutex()

    private val _state = MutableStateFlow(WorkerState())
    val state: StateFlow<WorkerState> = _state.asStateFlow()

    private var activeSitesCache: List<Site> = emptyList()

    val attendances: StateFlow<List<Attendance>> = authRepository.currentUser()
        .flatMapLatest { user ->
            if (user != null) {
                attendanceRepository.getUserAttendances(user.id)
            } else {
                flowOf(emptyList())
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val timeRemainingSeconds: StateFlow<Int> = attendances.flatMapLatest { atts ->
        flow {
            val last = atts.firstOrNull()
            if (last != null) {
                while (true) {
                    var diffMillis = android.os.SystemClock.elapsedRealtime() - last.elapsedRealtimeMillis
                    if (last.elapsedRealtimeMillis > android.os.SystemClock.elapsedRealtime()) {
                        diffMillis = System.currentTimeMillis() - last.deviceTimeMillis
                    }
                    val minDiffMillis = ValidateCheckInUseCase.MIN_MINUTES_BETWEEN_MARKS * 60 * 1000L
                    if (diffMillis < minDiffMillis) {
                        emit(((minDiffMillis - diffMillis) / 1000).toInt())
                        delay(1000)
                    } else {
                        emit(0)
                        break
                    }
                }
            } else {
                emit(0)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        viewModelScope.launch {
            authRepository.currentUser().collect { user ->
                _state.update { it.copy(user = user) }
            }
        }
        viewModelScope.launch {
            siteRepository.getActiveSitesFlow().collect { activeSites ->
                activeSitesCache = activeSites
                updateLocationAndSite(_state.value.currentLocation)
            }
        }
    }

    private fun updateLocationAndSite(location: Location?) {
        if (activeSitesCache.isEmpty()) {
            _state.update {
                it.copy(
                    site = null,
                    currentLocation = location,
                    distance = null,
                    validationResult = null,
                    errorMessage = application.getString(R.string.no_active_sites)
                )
            }
            return
        }

        if (location != null) {
            val closestSite = getClosestSiteUseCase(activeSitesCache, location.latitude, location.longitude)
            if (closestSite != null) {
                val isMock = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    location.isMock
                } else {
                    @Suppress("DEPRECATION")
                    location.isFromMockProvider
                }
                val (result, dist) = validateCheckInUseCase(
                    site = closestSite,
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracy = location.accuracy,
                    isMock = isMock,
                    lastAttendance = null,
                    currentElapsedRealtimeMillis = 0L
                )
                _state.update {
                    it.copy(
                        site = closestSite,
                        currentLocation = location,
                        distance = dist,
                        validationResult = result,
                        errorMessage = when (result) {
                            is ValidationResult.MockLocationDetected -> application.getString(R.string.error_mock_location)
                            is ValidationResult.LowAccuracy -> application.getString(R.string.error_low_accuracy)
                            is ValidationResult.OutOfRadius -> application.getString(R.string.error_out_of_radius)
                            is ValidationResult.TooSoon -> null
                            is ValidationResult.Valid -> null
                        }
                    )
                }
            } else {
                _state.update {
                    it.copy(
                        site = null,
                        currentLocation = location,
                        distance = null,
                        validationResult = null,
                        errorMessage = application.getString(R.string.no_active_sites)
                    )
                }
            }
        } else {
            // Default to first active site if location is not available yet
            val firstSite = activeSitesCache.firstOrNull()
            _state.update {
                it.copy(
                    site = firstSite,
                    errorMessage = if (firstSite == null) application.getString(R.string.no_active_sites) else null
                )
            }
        }
    }

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2000L)
            .setMinUpdateDistanceMeters(1f)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { loc ->
                    updateLocationAndSite(loc)
                }
            }
        }
        
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback!!,
            Looper.getMainLooper()
        )
    }

    fun stopLocationUpdates() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
        }
    }

    fun registerAttendance() {
        val currentState = _state.value
        if (currentState.isRegistering) return
        val user = currentState.user ?: return
        val site = currentState.site ?: return
        val loc = currentState.currentLocation ?: return

        viewModelScope.launch {
            if (mutex.isLocked) return@launch
            mutex.withLock {
                _state.update { it.copy(isRegistering = true) }
                val isMock = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    loc.isMock
                } else {
                    @Suppress("DEPRECATION")
                    loc.isFromMockProvider
                }
                
                val result = registerAttendanceUseCase(
                    userId = user.id,
                    site = site,
                    latitude = loc.latitude,
                    longitude = loc.longitude,
                    accuracy = loc.accuracy,
                    isMock = isMock
                )
                
                if (result !is ValidationResult.Valid) {
                    _state.update { 
                        it.copy(
                            isRegistering = false,
                            errorMessage = when (result) {
                                is ValidationResult.MockLocationDetected -> application.getString(R.string.error_mock_location)
                                is ValidationResult.LowAccuracy -> application.getString(R.string.error_low_accuracy)
                                is ValidationResult.OutOfRadius -> application.getString(R.string.error_out_of_radius)
                                is ValidationResult.TooSoon -> application.getString(R.string.error_too_soon, result.timeRemainingSeconds)
                                is ValidationResult.Valid -> null
                            }
                        ) 
                    }
                } else {
                    _state.update { it.copy(isRegistering = false) }
                }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}
