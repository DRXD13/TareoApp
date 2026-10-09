package com.dangeloretis.tareoapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dangeloretis.tareoapp.data.local.entity.SiteEntity
import kotlinx.coroutines.flow.Flow

@Dao
@JvmSuppressWildcards
interface SiteDao {
    @Query("SELECT * FROM sites")
    fun getAllSitesFlow(): Flow<List<SiteEntity>>

    @Query("SELECT * FROM sites WHERE isActive = 1 LIMIT 1")
    fun getFirstActiveSiteFlow(): Flow<SiteEntity?>

    @Query("SELECT * FROM sites WHERE isActive = 1 LIMIT 1")
    suspend fun getFirstActiveSite(): SiteEntity?

    @Query("SELECT * FROM sites WHERE id = :id")
    suspend fun getSiteById(id: String): SiteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSite(site: SiteEntity): Long

    @Update
    suspend fun updateSite(site: SiteEntity): Int
    
    @Query("SELECT COUNT(*) FROM sites WHERE isActive = 1")
    suspend fun getActiveSitesCount(): Int

    @Query("SELECT * FROM sites WHERE isActive = 1")
    fun getActiveSitesFlow(): Flow<List<SiteEntity>>

    @Query("DELETE FROM sites WHERE id = :id")
    suspend fun deleteSiteById(id: String): Int
}
