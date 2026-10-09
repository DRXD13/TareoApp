package com.dangeloretis.tareoapp.domain.repository

import com.dangeloretis.tareoapp.domain.model.Labor
import com.dangeloretis.tareoapp.domain.model.Lote
import kotlinx.coroutines.flow.Flow

interface CatalogRepository {
    fun getLabores(): Flow<List<Labor>>
    fun getLotes(): Flow<List<Lote>>
}
