package com.richyss.app.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Profiles are stored as a JSON payload plus the columns the UI queries and sorts
 * by. The JSON is the same document the user imports and exports, so a profile
 * survives schema changes to the SQL table without a migration.
 */
@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val isActive: Boolean,
    val updatedAt: Long,
    val payload: String
)
