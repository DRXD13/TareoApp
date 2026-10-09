package com.dangeloretis.tareoapp.data.repository

import com.dangeloretis.tareoapp.domain.model.User
import com.dangeloretis.tareoapp.domain.model.UserRole
import com.dangeloretis.tareoapp.domain.repository.AuthRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class DemoAuthRepository @Inject constructor() : AuthRepository {
    private val _currentUser = MutableStateFlow<User?>(null)

    // Simulando base de datos o API (DNI -> PIN to User)
    private val demoUsers = mapOf(
        "11111111" to Pair("1111", User("1", "11111111", "Juan Perez (Trabajador)", UserRole.WORKER)),
        "22222222" to Pair("2222", User("2", "22222222", "Carlos Gómez (Tareador)", UserRole.TAREADOR)),
        "33333333" to Pair("3333", User("3", "33333333", "Ana Silva (Admin)", UserRole.ADMIN))
    )

    override fun currentUser(): StateFlow<User?> = _currentUser.asStateFlow()

    override suspend fun login(dni: String, pin: String): Result<User> {
        delay(1000) // Simular retardo de red
        val credentials = demoUsers[dni]
        return if (credentials != null && credentials.first == pin) {
            _currentUser.value = credentials.second
            Result.success(credentials.second)
        } else {
            Result.failure(Exception("DNI o PIN incorrectos"))
        }
    }

    override suspend fun logout() {
        _currentUser.value = null
    }
}
