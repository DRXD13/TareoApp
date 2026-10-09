package com.dangeloretis.tareoapp.data.repository

import com.dangeloretis.tareoapp.domain.model.Labor
import com.dangeloretis.tareoapp.domain.model.Lote
import com.dangeloretis.tareoapp.domain.repository.CatalogRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class DemoCatalogRepository @Inject constructor() : CatalogRepository {
    private val labores = listOf(
        Labor("1", "Cosecha"),
        Labor("2", "Poda"),
        Labor("3", "Riego"),
        Labor("4", "Fumigación"),
        Labor("5", "Empaque")
    )

    private val lotes = listOf(
        Lote("1", "Lote A1"),
        Lote("2", "Lote A2"),
        Lote("3", "Lote B1"),
        Lote("4", "Lote B2")
    )

    override fun getLabores(): Flow<List<Labor>> = flowOf(labores)

    override fun getLotes(): Flow<List<Lote>> = flowOf(lotes)
}
