package com.dangeloretis.tareoapp.domain.usecase

import com.dangeloretis.tareoapp.domain.model.Site
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class GetClosestSiteUseCaseTest {

    private lateinit var calculateDistanceUseCase: CalculateDistanceUseCase
    private lateinit var getClosestSiteUseCase: GetClosestSiteUseCase

    @Before
    fun setup() {
        calculateDistanceUseCase = CalculateDistanceUseCase()
        getClosestSiteUseCase = GetClosestSiteUseCase(calculateDistanceUseCase)
    }

    @Test
    fun `getClosestSite with empty list returns null`() {
        val result = getClosestSiteUseCase(emptyList(), -12.0, -77.0)
        assertNull(result)
    }

    @Test
    fun `getClosestSite with only inactive sites returns null`() {
        val site1 = Site("1", "Sede 1", -12.0, -77.0, 100f, isActive = false)
        val result = getClosestSiteUseCase(listOf(site1), -12.0, -77.0)
        assertNull(result)
    }

    @Test
    fun `getClosestSite returns the active site closest to user location`() {
        // User at (-12.0000, -77.0000)
        val closeSite = Site("1", "Sede Cerca", -12.0010, -77.0010, 100f, isActive = true)
        val farSite = Site("2", "Sede Lejos", -12.0500, -77.0500, 100f, isActive = true)
        val inactiveClosestSite = Site("3", "Sede Inactiva Mas Cerca", -12.0001, -77.0001, 100f, isActive = false)

        val sites = listOf(farSite, closeSite, inactiveClosestSite)
        
        val result = getClosestSiteUseCase(sites, -12.0000, -77.0000)

        assertEquals("1", result?.id)
        assertEquals("Sede Cerca", result?.name)
    }
}
