package com.richyss.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ProfileEntity::class],
    version = 1,
    exportSchema = true
)
abstract class RichyssDatabase : RoomDatabase() {

    abstract fun profileDao(): ProfileDao

    companion object {
        fun build(context: Context): RichyssDatabase =
            Room.databaseBuilder(context, RichyssDatabase::class.java, "richyss.db").build()
    }
}
