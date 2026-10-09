package com.dangeloretis.tareoapp.data.repository

import com.dangeloretis.tareoapp.data.local.dao.SiteDao
import com.dangeloretis.tareoapp.data.local.entity.SiteEntity
import com.dangeloretis.tareoapp.domain.model.Site
import com.dangeloretis.tareoapp.domain.repository.SiteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RoomSiteRepository @Inject constructor(
    private val siteDao: SiteDao
) : SiteRepository {

    override suspend fun getDefaultSite(): Site {
        val entity = siteDao.getFirstActiveSite()
        return entity?.toDomain() ?: Site(
            id = "demo_site_1",
            name = "Sede Central Demo",
            latitude = 37.4220,
            longitude = -122.0841,
            radiusMeters = 100f,
            isActive = true
        )
    }

    override fun getActiveSiteFlow(): Flow<Site?> {
        return siteDao.getFirstActiveSiteFlow().map { it?.toDomain() }
    }

    override fun getActiveSitesFlow(): Flow<List<Site>> {
        return siteDao.getActiveSitesFlow().map { list -> list.map { it.toDomain() } }
    }

    override fun getAllSitesFlow(): Flow<List<Site>> {
        return siteDao.getAllSitesFlow().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getSiteById(id: String): Site? {
        return siteDao.getSiteById(id)?.toDomain()
    }

    override suspend fun saveSite(site: Site) {
        siteDao.insertSite(site.toEntity())
    }

    override suspend fun deleteSite(id: String) {
        siteDao.deleteSiteById(id)
    }

    override suspend fun getActiveSitesCount(): Int {
        return siteDao.getActiveSitesCount()
    }
}

fun SiteEntity.toDomain() = Site(
    id = id,
    name = name,
    latitude = latitude,
    longitude = longitude,
    radiusMeters = radiusMeters,
    isActive = isActive
)

fun Site.toEntity() = SiteEntity(
    id = id,
    name = name,
    latitude = latitude,
    longitude = longitude,
    radiusMeters = radiusMeters,
    isActive = isActive
)
