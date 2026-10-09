package com.dangeloretis.tareoapp.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.AutoMigration
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.dangeloretis.tareoapp.data.local.dao.AttendanceDao
import com.dangeloretis.tareoapp.data.local.dao.CrewTareoDao
import com.dangeloretis.tareoapp.data.local.dao.SiteDao
import com.dangeloretis.tareoapp.data.local.entity.AttendanceEntity
import com.dangeloretis.tareoapp.data.local.entity.CrewTareoEntity
import com.dangeloretis.tareoapp.data.local.entity.SiteEntity

@Database(
    entities = [AttendanceEntity::class, CrewTareoEntity::class, SiteEntity::class],
    version = 3,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]
)
@TypeConverters(Converters::class)
abstract class TareoDatabase : RoomDatabase() {
    abstract fun attendanceDao(): AttendanceDao
    abstract fun crewTareoDao(): CrewTareoDao
    abstract fun siteDao(): SiteDao

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `sites` (
                        `id` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `latitude` REAL NOT NULL,
                        `longitude` REAL NOT NULL,
                        `radiusMeters` REAL NOT NULL,
                        `isActive` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO `sites` (`id`, `name`, `latitude`, `longitude`, `radiusMeters`, `isActive`)
                    VALUES ('demo_site_1', 'Sede Central Demo', 37.4220, -122.0841, 100.0, 1)
                    """.trimIndent()
                )
            }
        }
    }
}
