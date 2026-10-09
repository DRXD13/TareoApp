package com.dangeloretis.tareoapp.domain.model

data class Site(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val radiusMeters: Float,
    val isActive: Boolean = true
)
