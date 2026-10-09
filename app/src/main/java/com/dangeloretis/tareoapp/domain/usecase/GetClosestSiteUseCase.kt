package com.dangeloretis.tareoapp.domain.usecase

import com.dangeloretis.tareoapp.domain.model.Site
import javax.inject.Inject

class GetClosestSiteUseCase @Inject constructor(
    private val calculateDistanceUseCase: CalculateDistanceUseCase
) {
    operator fun invoke(
        sites: List<Site>,
        latitude: Double,
        longitude: Double
    ): Site? {
        val activeSites = sites.filter { it.isActive }
        if (activeSites.isEmpty()) return null

        return activeSites.minByOrNull { site ->
            calculateDistanceUseCase(
                lat1 = site.latitude,
                lon1 = site.longitude,
                lat2 = latitude,
                lon2 = longitude
            )
        }
    }
}
