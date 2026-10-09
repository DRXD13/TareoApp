package com.dangeloretis.tareoapp.domain.repository

import com.dangeloretis.tareoapp.domain.model.Site
import kotlinx.coroutines.flow.Flow

interface SiteRepository {
    suspend fun getDefaultSite(): Site
    fun getActiveSiteFlow(): Flow<Site?>
    fun getActiveSitesFlow(): Flow<List<Site>>
    fun getAllSitesFlow(): Flow<List<Site>>
    suspend fun getSiteById(id: String): Site?
    suspend fun saveSite(site: Site)
    suspend fun deleteSite(id: String)
    suspend fun getActiveSitesCount(): Int
}
