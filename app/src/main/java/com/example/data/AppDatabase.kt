package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.PlaylistDao
import com.example.data.dao.SongDao
import com.example.data.dao.UserDao
import com.example.data.model.PlaylistEntity
import com.example.data.model.PlaylistSongCrossRef
import com.example.data.model.SongEntity
import com.example.data.model.UserEntity

@Database(
    entities = [
        UserEntity::class,
        SongEntity::class,
        PlaylistEntity::class,
        PlaylistSongCrossRef::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun songDao(): SongDao
    abstract fun playlistDao(): PlaylistDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "sound_vault.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
