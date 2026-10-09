package com.dangeloretis.tareoapp.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.AutoMigration
import com.dangeloretis.tareoapp.data.local.dao.AttendanceDao
import com.dangeloretis.tareoapp.data.local.dao.CrewTareoDao
import com.dangeloretis.tareoapp.data.local.entity.AttendanceEntity
import com.dangeloretis.tareoapp.data.local.entity.CrewTareoEntity

@Database(
    entities = [AttendanceEntity::class, CrewTareoEntity::class],
    version = 2,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]
)
@TypeConverters(Converters::class)
abstract class TareoDatabase : RoomDatabase() {
    abstract fun attendanceDao(): AttendanceDao
    abstract fun crewTareoDao(): CrewTareoDao
}
