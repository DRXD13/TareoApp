package com.dangeloretis.tareoapp.domain.usecase

import com.dangeloretis.tareoapp.domain.model.Site
import com.dangeloretis.tareoapp.domain.repository.SiteRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class SaveSiteUseCaseTest {

    private lateinit var siteRepository: SiteRepository
    private lateinit var saveSiteUseCase: SaveSiteUseCase

    @Before
    fun setup() {
        siteRepository = mock(SiteRepository::class.java)
        saveSiteUseCase = SaveSiteUseCase(siteRepository)
    }

    @Test
    fun saveSiteWithValidDataReturnsSuccess() = runBlocking {
        val site = Site("1", "Valid Site", 10.0, 20.0, 100f, true)
        
        saveSiteUseCase(site)
        
        verify(siteRepository).saveSite(site)
    }

    @Test
    fun saveSiteWithBlankNameThrowsException() = runBlocking {
        val site = Site("1", "  ", 10.0, 20.0, 100f, true)
        
        try {
            saveSiteUseCase(site)
            fail("Expected exception")
        } catch (e: IllegalArgumentException) {
            assertEquals("El nombre es obligatorio.", e.message)
        }
    }

    @Test
    fun saveSiteWithInvalidLatitudeThrowsException() = runBlocking {
        val site = Site("1", "Valid Site", 91.0, 20.0, 100f, true)
        
        try {
            saveSiteUseCase(site)
            fail("Expected exception")
        } catch (e: IllegalArgumentException) {
            assertEquals("Latitud inválida. Debe estar entre -90 y 90.", e.message)
        }
    }
    
    @Test
    fun saveSiteWithInvalidRadiusThrowsException() = runBlocking {
        val site = Site("1", "Valid Site", 10.0, 20.0, 10f, true)
        
        try {
            saveSiteUseCase(site)
            fail("Expected exception")
        } catch (e: IllegalArgumentException) {
            assertEquals("Radio inválido. Debe estar entre 20 y 1000 metros.", e.message)
        }
    }

    @Test
    fun saveSiteDeactivatingLastActiveSiteThrowsException() = runBlocking {
        val activeSite = Site("1", "Valid Site", 10.0, 20.0, 100f, true)
        val siteToSave = activeSite.copy(isActive = false)
        
        `when`(siteRepository.getActiveSitesCount()).thenReturn(1)
        `when`(siteRepository.getSiteById("1")).thenReturn(activeSite)
        
        try {
            saveSiteUseCase(siteToSave)
            fail("Expected exception")
        } catch (e: IllegalArgumentException) {
            assertEquals("No se puede desactivar la última sede activa.", e.message)
        }
    }
}
