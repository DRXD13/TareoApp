package com.dangeloretis.tareoapp.domain.usecase

import com.dangeloretis.tareoapp.domain.repository.AttendanceRepository
import com.dangeloretis.tareoapp.domain.repository.SiteRepository
import javax.inject.Inject

class DeleteSiteUseCase @Inject constructor(
    private val siteRepository: SiteRepository,
    private val attendanceRepository: AttendanceRepository
) {
    suspend operator fun invoke(siteId: String) {
        val site = siteRepository.getSiteById(siteId) ?: return

        if (site.isActive) {
            val activeCount = siteRepository.getActiveSitesCount()
            if (activeCount == 1) {
                throw IllegalArgumentException("No se puede eliminar la última sede activa. Solo se puede editar o agregar otra sede.")
            }
        }

        val attendancesCount = attendanceRepository.getAttendancesCountForSite(siteId)
        if (attendancesCount > 0) {
            throw IllegalArgumentException("No se puede eliminar una sede con marcajes asociados. Solo se puede desactivar.")
        }

        siteRepository.deleteSite(siteId)
    }
}
