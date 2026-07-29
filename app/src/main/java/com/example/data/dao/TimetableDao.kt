package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.TimetableSlotEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TimetableDao {

    @Query("SELECT * FROM timetable_slots ORDER BY id ASC")
    fun getAllSlots(): Flow<List<TimetableSlotEntity>>

    @Query("SELECT * FROM timetable_slots WHERE dayOfWeek = :dayOfWeek AND timeSlot = :timeSlot LIMIT 1")
    suspend fun getSlotByDayAndTime(dayOfWeek: String, timeSlot: String): TimetableSlotEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateSlot(slot: TimetableSlotEntity): Long

    @Query("DELETE FROM timetable_slots WHERE dayOfWeek = :dayOfWeek AND timeSlot = :timeSlot")
    suspend fun deleteSlotByDayAndTime(dayOfWeek: String, timeSlot: String)

    @Query("DELETE FROM timetable_slots")
    suspend fun clearAllSlots()
}
