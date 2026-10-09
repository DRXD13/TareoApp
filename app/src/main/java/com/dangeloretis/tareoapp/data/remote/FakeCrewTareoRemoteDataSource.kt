package com.dangeloretis.tareoapp.data.remote

import com.dangeloretis.tareoapp.data.local.entity.CrewTareoEntity
import kotlinx.coroutines.delay
import javax.inject.Inject

class FakeCrewTareoRemoteDataSource @Inject constructor() : CrewTareoRemoteDataSource {
    override suspend fun syncTareo(tareo: CrewTareoEntity): Result<Unit> {
        delay(2000)
        return Result.success(Unit)
    }
}
