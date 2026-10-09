package com.dangeloretis.tareoapp.domain.model

data class User(
    val id: String,
    val dni: String,
    val fullName: String,
    val role: UserRole
)
