package com.dangeloretis.tareoapp.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.dangeloretis.tareoapp.data.local.dao.AttendanceDao
import com.dangeloretis.tareoapp.data.local.entity.AttendanceEntity

@Database(entities = [AttendanceEntity::class], version = 1, exportSchema = true)
@TypeConverters(Converters::class)
abstract class TareoDatabase : RoomDatabase() {
    abstract fun attendanceDao(): AttendanceDao
}
