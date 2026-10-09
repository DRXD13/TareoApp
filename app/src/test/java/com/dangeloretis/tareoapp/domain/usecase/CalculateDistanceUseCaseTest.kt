package com.dangeloretis.tareoapp.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class CalculateDistanceUseCaseTest {

    private val calculateDistanceUseCase = CalculateDistanceUseCase()

    @Test
    fun `distance between same points should be 0`() {
        val distance = calculateDistanceUseCase(37.4220, -122.0841, 37.4220, -122.0841)
        assertEquals(0f, distance, 0.1f)
    }
}
