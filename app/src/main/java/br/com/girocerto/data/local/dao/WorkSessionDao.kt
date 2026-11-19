package br.com.girocerto.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.com.girocerto.data.local.entity.WorkSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkSessionDao {

    @Query("SELECT startedAtEpochMillis FROM work_sessions WHERE dateKey = :dateKey LIMIT 1")
    fun observeStartedAt(dateKey: String): Flow<Long?>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(session: WorkSessionEntity)
}