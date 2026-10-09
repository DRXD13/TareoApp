package com.dangeloretis.tareoapp.domain.usecase

import com.dangeloretis.tareoapp.domain.model.Site
import com.dangeloretis.tareoapp.domain.repository.AttendanceRepository
import com.dangeloretis.tareoapp.domain.repository.SiteRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class DeleteSiteUseCaseTest {

    private lateinit var siteRepository: SiteRepository
    private lateinit var attendanceRepository: AttendanceRepository
    private lateinit var deleteSiteUseCase: DeleteSiteUseCase

    @Before
    fun setup() {
        siteRepository = mock(SiteRepository::class.java)
        attendanceRepository = mock(AttendanceRepository::class.java)
        deleteSiteUseCase = DeleteSiteUseCase(siteRepository, attendanceRepository)
    }

    @Test
    fun `deleteSite successful when inactive and has no attendances`() = runBlocking {
        val site = Site("site_1", "Sede Inactiva", 10.0, 20.0, 100f, isActive = false)
        `when`(siteRepository.getSiteById("site_1")).thenReturn(site)
        `when`(attendanceRepository.getAttendancesCountForSite("site_1")).thenReturn(0)

        deleteSiteUseCase("site_1")

        verify(siteRepository).deleteSite("site_1")
    }

    @Test
    fun `deleteSite throws exception when it is the last active site`() = runBlocking {
        val site = Site("site_1", "Sede Activa", 10.0, 20.0, 100f, isActive = true)
        `when`(siteRepository.getSiteById("site_1")).thenReturn(site)
        `when`(siteRepository.getActiveSitesCount()).thenReturn(1)

        try {
            deleteSiteUseCase("site_1")
            fail("Expected exception")
        } catch (e: IllegalArgumentException) {
            assertEquals("No se puede eliminar la última sede activa. Solo se puede editar o agregar otra sede.", e.message)
        }
    }

    @Test
    fun `deleteSite throws exception when site has attendances`() = runBlocking {
        val site = Site("site_1", "Sede Inactiva", 10.0, 20.0, 100f, isActive = false)
        `when`(siteRepository.getSiteById("site_1")).thenReturn(site)
        `when`(attendanceRepository.getAttendancesCountForSite("site_1")).thenReturn(5)

        try {
            deleteSiteUseCase("site_1")
            fail("Expected exception")
        } catch (e: IllegalArgumentException) {
            assertEquals("No se puede eliminar una sede con marcajes asociados. Solo se puede desactivar.", e.message)
        }
    }
}
