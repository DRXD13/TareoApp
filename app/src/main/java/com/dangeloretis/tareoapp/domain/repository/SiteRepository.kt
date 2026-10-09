package com.dangeloretis.tareoapp.domain.repository

import com.dangeloretis.tareoapp.domain.model.Site

interface SiteRepository {
    suspend fun getDefaultSite(): Site
}
