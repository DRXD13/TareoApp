package com.dangeloretis.tareoapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dangeloretis.tareoapp.data.local.entity.AttendanceEntity
import com.dangeloretis.tareoapp.data.local.entity.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
@JvmSuppressWildcards
interface AttendanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(attendance: AttendanceEntity): Long

    @Query("SELECT * FROM attendances WHERE userId = :userId ORDER BY deviceTimeMillis DESC")
    fun getAttendances(userId: String): Flow<List<AttendanceEntity>>

    @Query("SELECT * FROM attendances WHERE userId = :userId ORDER BY deviceTimeMillis DESC, id DESC LIMIT 1")
    fun getLastAttendance(userId: String): AttendanceEntity?

    @Query("SELECT * FROM attendances WHERE syncStatus IN (:statuses)")
    fun getAttendancesBySyncStatus(statuses: List<SyncStatus>): List<AttendanceEntity>

    @Query("UPDATE attendances SET syncStatus = :status, syncedAtMillis = :syncedAt WHERE id IN (:ids)")
    fun updateSyncStatus(ids: List<String>, status: SyncStatus, syncedAt: Long?): Int
}
