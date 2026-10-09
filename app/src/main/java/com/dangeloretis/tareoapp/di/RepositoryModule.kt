package com.dangeloretis.tareoapp.di

import com.dangeloretis.tareoapp.data.remote.AttendanceRemoteDataSource
import com.dangeloretis.tareoapp.data.remote.FakeAttendanceRemoteDataSource
import com.dangeloretis.tareoapp.data.repository.DemoAuthRepository
import com.dangeloretis.tareoapp.data.repository.DemoSiteRepository
import com.dangeloretis.tareoapp.data.repository.RoomAttendanceRepository
import com.dangeloretis.tareoapp.domain.repository.AuthRepository
import com.dangeloretis.tareoapp.domain.repository.AttendanceRepository
import com.dangeloretis.tareoapp.domain.repository.SiteRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        demoAuthRepository: DemoAuthRepository
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindSiteRepository(
        demoSiteRepository: DemoSiteRepository
    ): SiteRepository

    @Binds
    @Singleton
    abstract fun bindAttendanceRepository(
        roomAttendanceRepository: RoomAttendanceRepository
    ): AttendanceRepository
    @Binds
    @Singleton
    abstract fun bindAttendanceRemoteDataSource(
        fakeAttendanceRemoteDataSource: FakeAttendanceRemoteDataSource
    ): AttendanceRemoteDataSource
    @Binds
    @Singleton
    abstract fun bindWorkerDirectoryRepository(
        demoWorkerDirectoryRepository: com.dangeloretis.tareoapp.data.repository.DemoWorkerDirectoryRepository
    ): com.dangeloretis.tareoapp.domain.repository.WorkerDirectoryRepository

    @Binds
    @Singleton
    abstract fun bindCatalogRepository(
        demoCatalogRepository: com.dangeloretis.tareoapp.data.repository.DemoCatalogRepository
    ): com.dangeloretis.tareoapp.domain.repository.CatalogRepository
    @Binds
    @Singleton
    abstract fun bindCrewTareoRepository(
        roomCrewTareoRepository: com.dangeloretis.tareoapp.data.repository.RoomCrewTareoRepository
    ): com.dangeloretis.tareoapp.domain.repository.CrewTareoRepository
    @Binds
    @Singleton
    abstract fun bindCrewTareoRemoteDataSource(
        fakeCrewTareoRemoteDataSource: com.dangeloretis.tareoapp.data.remote.FakeCrewTareoRemoteDataSource
    ): com.dangeloretis.tareoapp.data.remote.CrewTareoRemoteDataSource
}
