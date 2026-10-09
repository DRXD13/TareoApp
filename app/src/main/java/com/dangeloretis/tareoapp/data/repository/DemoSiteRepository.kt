package com.dangeloretis.tareoapp.data.repository

import com.dangeloretis.tareoapp.domain.model.Site
import com.dangeloretis.tareoapp.domain.repository.SiteRepository
import javax.inject.Inject

class DemoSiteRepository @Inject constructor() : SiteRepository {
    override suspend fun getDefaultSite(): Site {
        return Site(
            id = "demo_site_1",
            name = "Sede Central Demo",
            latitude = 37.4220,
            longitude = -122.0841,
            radiusMeters = 100f
        )
    }
}
