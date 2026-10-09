package com.dangeloretis.tareoapp.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dangeloretis.tareoapp.domain.model.User
import com.dangeloretis.tareoapp.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginState(
    val dni: String = "",
    val pin: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val successUser: User? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun onDniChange(newDni: String) {
        if (newDni.length <= 8) {
            _state.update { it.copy(dni = newDni, error = null) }
        }
    }

    fun onPinChange(newPin: String) {
        if (newPin.length <= 4) {
            _state.update { it.copy(pin = newPin, error = null) }
        }
    }

    fun onDemoUserSelected(dni: String, pin: String) {
        _state.update { it.copy(dni = dni, pin = pin, error = null) }
    }

    fun login() {
        val currentState = _state.value
        if (currentState.dni.length != 8) {
            _state.update { it.copy(error = "El DNI debe tener 8 dígitos") }
            return
        }
        if (currentState.pin.length != 4) {
            _state.update { it.copy(error = "El PIN debe tener 4 dígitos") }
            return
        }

        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            val result = authRepository.login(currentState.dni, currentState.pin)
            result.onSuccess { user ->
                _state.update { it.copy(isLoading = false, successUser = user) }
            }.onFailure { error ->
                _state.update { it.copy(isLoading = false, error = error.message) }
            }
        }
    }
    
    fun resetSuccessState() {
        _state.update { it.copy(successUser = null) }
    }
}
