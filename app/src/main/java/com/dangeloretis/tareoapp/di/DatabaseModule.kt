package com.dangeloretis.tareoapp.di

import android.content.Context
import androidx.room.Room
import com.dangeloretis.tareoapp.data.local.dao.AttendanceDao
import com.dangeloretis.tareoapp.data.local.db.TareoDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideTareoDatabase(
        @ApplicationContext context: Context
    ): TareoDatabase {
        return Room.databaseBuilder(
            context,
            TareoDatabase::class.java,
            "tareo_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideAttendanceDao(database: TareoDatabase): AttendanceDao {
        return database.attendanceDao()
    }
}
