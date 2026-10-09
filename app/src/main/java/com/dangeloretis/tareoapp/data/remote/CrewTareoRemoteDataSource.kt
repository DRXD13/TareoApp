package com.dangeloretis.tareoapp.data.remote

import com.dangeloretis.tareoapp.data.local.entity.CrewTareoEntity

interface CrewTareoRemoteDataSource {
    suspend fun syncTareo(tareo: CrewTareoEntity): Result<Unit>
}
