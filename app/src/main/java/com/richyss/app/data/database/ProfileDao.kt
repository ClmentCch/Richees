package com.richyss.app.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {

    @Query("SELECT * FROM profiles ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM profiles ORDER BY updatedAt DESC LIMIT :limit")
    suspend fun recent(limit: Int): List<ProfileEntity>

    @Query("SELECT * FROM profiles WHERE isActive = 1 LIMIT 1")
    fun observeActive(): Flow<ProfileEntity?>

    @Query("SELECT * FROM profiles WHERE id = :id")
    suspend fun findById(id: String): ProfileEntity?

    @Query("SELECT COUNT(*) FROM profiles")
    fun observeCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: ProfileEntity)

    @Query("DELETE FROM profiles WHERE id = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM profiles")
    suspend fun deleteAll()

    @Query("UPDATE profiles SET isActive = 0")
    suspend fun clearActive()

    @Query("UPDATE profiles SET isActive = 1 WHERE id = :id")
    suspend fun markActive(id: String)
}
