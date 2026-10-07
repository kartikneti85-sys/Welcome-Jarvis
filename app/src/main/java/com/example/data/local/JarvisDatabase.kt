package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.model.IntelNote
import kotlinx.coroutines.flow.Flow

@Dao
interface IntelNoteDao {
    @Query("SELECT * FROM intel_notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<IntelNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: IntelNote): Long

    @Delete
    suspend fun deleteNote(note: IntelNote)

    @Query("DELETE FROM intel_notes")
    suspend fun clearAll()
}

@Database(entities = [IntelNote::class], version = 1, exportSchema = false)
abstract class JarvisDatabase : RoomDatabase() {
    abstract fun intelNoteDao(): IntelNoteDao

    companion object {
        @Volatile
        private var INSTANCE: JarvisDatabase? = null

        fun getInstance(context: Context): JarvisDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JarvisDatabase::class.java,
                    "jarvis_intel.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
