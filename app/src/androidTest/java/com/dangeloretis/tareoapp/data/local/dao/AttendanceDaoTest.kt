package com.dangeloretis.tareoapp.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dangeloretis.tareoapp.data.local.db.TareoDatabase
import com.dangeloretis.tareoapp.data.local.entity.AttendanceEntity
import com.dangeloretis.tareoapp.data.local.entity.SyncStatus
import com.dangeloretis.tareoapp.domain.model.AttendanceType
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AttendanceDaoTest {
    private lateinit var db: TareoDatabase
    private lateinit var dao: AttendanceDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, TareoDatabase::class.java).build()
        dao = db.attendanceDao()
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndRetrieveAttendance() = runBlocking {
        val entity = AttendanceEntity(
            id = "1", userId = "user1", siteId = "site1", type = AttendanceType.CHECK_IN,
            latitude = 0.0, longitude = 0.0, accuracyMeters = 0f, deviceTimeMillis = 1000L,
            elapsedRealtimeMillis = 0L, isMockLocation = false, distanceMeters = 0f,
            syncStatus = SyncStatus.PENDING
        )

        dao.insert(entity)

        val retrieved = dao.getLastAttendance("user1")
        assertNotNull(retrieved)
        assertEquals("1", retrieved?.id)
        assertEquals(SyncStatus.PENDING, retrieved?.syncStatus)
    }

    @Test
    fun getAttendancesBySyncStatusAndUpdate() = runBlocking {
        val entity1 = AttendanceEntity(
            id = "1", userId = "user1", siteId = "site1", type = AttendanceType.CHECK_IN,
            latitude = 0.0, longitude = 0.0, accuracyMeters = 0f, deviceTimeMillis = 1000L,
            elapsedRealtimeMillis = 0L, isMockLocation = false, distanceMeters = 0f,
            syncStatus = SyncStatus.PENDING
        )
        val entity2 = AttendanceEntity(
            id = "2", userId = "user1", siteId = "site1", type = AttendanceType.CHECK_IN,
            latitude = 0.0, longitude = 0.0, accuracyMeters = 0f, deviceTimeMillis = 2000L,
            elapsedRealtimeMillis = 0L, isMockLocation = false, distanceMeters = 0f,
            syncStatus = SyncStatus.FAILED
        )

        dao.insert(entity1)
        dao.insert(entity2)

        val pendingAndFailed = dao.getAttendancesBySyncStatus(listOf(SyncStatus.PENDING, SyncStatus.FAILED))
        assertEquals(2, pendingAndFailed.size)

        dao.updateSyncStatus(listOf("1", "2"), SyncStatus.SYNCED, 5000L)

        val synced = dao.getAttendancesBySyncStatus(listOf(SyncStatus.SYNCED))
        assertEquals(2, synced.size)
        assertEquals(5000L, synced[0].syncedAtMillis)
        
        val stillPending = dao.getAttendancesBySyncStatus(listOf(SyncStatus.PENDING))
        assertTrue(stillPending.isEmpty())
    }

    @Test
    fun getLastAttendanceReturnsMostRecent() = runBlocking {
        val entity1 = AttendanceEntity(
            id = "1", userId = "user2", siteId = "site1", type = AttendanceType.CHECK_IN,
            latitude = 0.0, longitude = 0.0, accuracyMeters = 0f, deviceTimeMillis = 1000L,
            elapsedRealtimeMillis = 0L, isMockLocation = false, distanceMeters = 0f,
            syncStatus = SyncStatus.PENDING
        )
        val entity2 = AttendanceEntity(
            id = "2", userId = "user2", siteId = "site1", type = AttendanceType.CHECK_OUT,
            latitude = 0.0, longitude = 0.0, accuracyMeters = 0f, deviceTimeMillis = 3000L,
            elapsedRealtimeMillis = 0L, isMockLocation = false, distanceMeters = 0f,
            syncStatus = SyncStatus.PENDING
        )
        val entity3 = AttendanceEntity(
            id = "3", userId = "user2", siteId = "site1", type = AttendanceType.CHECK_IN,
            latitude = 0.0, longitude = 0.0, accuracyMeters = 0f, deviceTimeMillis = 2000L,
            elapsedRealtimeMillis = 0L, isMockLocation = false, distanceMeters = 0f,
            syncStatus = SyncStatus.PENDING
        )
        val otherUserEntity = AttendanceEntity(
            id = "4", userId = "user3", siteId = "site1", type = AttendanceType.CHECK_IN,
            latitude = 0.0, longitude = 0.0, accuracyMeters = 0f, deviceTimeMillis = 5000L,
            elapsedRealtimeMillis = 0L, isMockLocation = false, distanceMeters = 0f,
            syncStatus = SyncStatus.PENDING
        )

        // Insert in scrambled order
        dao.insert(entity1)
        dao.insert(otherUserEntity)
        dao.insert(entity3)
        dao.insert(entity2)

        val lastAttendance = dao.getLastAttendance("user2")
        assertNotNull(lastAttendance)
        // Entity2 has the highest deviceTimeMillis (3000L) for user2
        assertEquals("2", lastAttendance?.id)
        assertEquals(3000L, lastAttendance?.deviceTimeMillis)
    }
}
