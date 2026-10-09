package com.dangeloretis.tareoapp.presentation.tareador

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dangeloretis.tareoapp.data.local.entity.CrewTareoEntity
import com.dangeloretis.tareoapp.domain.model.Labor
import com.dangeloretis.tareoapp.domain.model.Lote
import com.dangeloretis.tareoapp.domain.repository.AuthRepository
import com.dangeloretis.tareoapp.domain.repository.CatalogRepository
import com.dangeloretis.tareoapp.domain.repository.CrewTareoRepository
import com.dangeloretis.tareoapp.domain.usecase.RegisterCrewTareoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TareadorViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val catalogRepository: CatalogRepository,
    private val crewTareoRepository: CrewTareoRepository,
    private val registerCrewTareoUseCase: RegisterCrewTareoUseCase
) : ViewModel() {

    val labores: StateFlow<List<Labor>> = catalogRepository.getLabores()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lotes: StateFlow<List<Lote>> = catalogRepository.getLotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _uiState = MutableStateFlow(TareadorUiState())
    val uiState: StateFlow<TareadorUiState> = _uiState.asStateFlow()

    private val tareadorId: String
        get() = authRepository.currentUser().value?.id ?: ""

    init {
        viewModelScope.launch {
            crewTareoRepository.getTareosForTareador(tareadorId).collect { tareos ->
                _uiState.update { it.copy(tareos = tareos) }
            }
        }
    }

    fun onLaborSelected(labor: Labor) {
        _uiState.update { it.copy(selectedLabor = labor) }
    }

    fun onLoteSelected(lote: Lote) {
        _uiState.update { it.copy(selectedLote = lote) }
    }

    fun onDniChanged(dni: String) {
        _uiState.update { it.copy(dniInput = dni) }
    }

    fun registerTareo(method: String = "DNI", scannedDni: String? = null) {
        val state = uiState.value
        val laborId = state.selectedLabor?.id ?: ""
        val loteId = state.selectedLote?.id ?: ""
        val dniToRegister = scannedDni ?: state.dniInput

        viewModelScope.launch {
            val result = registerCrewTareoUseCase(tareadorId, dniToRegister, laborId, loteId, method)
            if (result.isSuccess) {
                val tareo = result.getOrThrow()
                _uiState.update { it.copy(
                    message = "Trabajador registrado: ${tareo.workerName}",
                    dniInput = if (method == "DNI") "" else it.dniInput
                ) }
            } else {
                _uiState.update { it.copy(message = result.exceptionOrNull()?.message ?: "Error desconocido") }
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}

data class TareadorUiState(
    val selectedLabor: Labor? = null,
    val selectedLote: Lote? = null,
    val dniInput: String = "",
    val tareos: List<CrewTareoEntity> = emptyList(),
    val message: String? = null
)
