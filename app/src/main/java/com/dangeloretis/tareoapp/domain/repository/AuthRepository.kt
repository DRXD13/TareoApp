package com.dangeloretis.tareoapp.domain.repository

import com.dangeloretis.tareoapp.domain.model.User
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    fun currentUser(): StateFlow<User?>
    suspend fun login(dni: String, pin: String): Result<User>
    suspend fun logout()
}
