package com.dangeloretis.tareoapp.data.local.db

import androidx.room.TypeConverter
import com.dangeloretis.tareoapp.data.local.entity.SyncStatus
import com.dangeloretis.tareoapp.domain.model.AttendanceType

class Converters {
    @TypeConverter
    fun fromSyncStatus(status: SyncStatus): String {
        return status.name
    }

    @TypeConverter
    fun toSyncStatus(status: String): SyncStatus {
        return SyncStatus.valueOf(status)
    }

    @TypeConverter
    fun fromAttendanceType(type: AttendanceType): String {
        return type.name
    }

    @TypeConverter
    fun toAttendanceType(type: String): AttendanceType {
        return AttendanceType.valueOf(type)
    }
}
