package com.dangeloretis.tareoapp.domain.usecase

import com.dangeloretis.tareoapp.domain.model.Site
import com.dangeloretis.tareoapp.domain.repository.SiteRepository
import javax.inject.Inject

class SaveSiteUseCase @Inject constructor(
    private val siteRepository: SiteRepository
) {
    suspend operator fun invoke(site: Site) {
        if (site.name.isBlank()) {
            throw IllegalArgumentException("El nombre es obligatorio.")
        }
        if (site.latitude < -90.0 || site.latitude > 90.0) {
            throw IllegalArgumentException("Latitud inválida. Debe estar entre -90 y 90.")
        }
        if (site.longitude < -180.0 || site.longitude > 180.0) {
            throw IllegalArgumentException("Longitud inválida. Debe estar entre -180 y 180.")
        }
        if (site.radiusMeters < 20f || site.radiusMeters > 1000f) {
            throw IllegalArgumentException("Radio inválido. Debe estar entre 20 y 1000 metros.")
        }

        if (!site.isActive) {
            val activeCount = siteRepository.getActiveSitesCount()
            if (activeCount == 1) {
                val current = siteRepository.getSiteById(site.id)
                if (current != null && current.isActive) {
                    throw IllegalArgumentException("No se puede desactivar la última sede activa.")
                }
            }
        }

        siteRepository.saveSite(site)
    }
}
