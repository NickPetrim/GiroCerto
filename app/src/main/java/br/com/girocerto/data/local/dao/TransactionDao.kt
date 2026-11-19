package br.com.girocerto.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.com.girocerto.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    @Query(
        """
        SELECT * FROM transactions
        WHERE occurredAtEpochMillis >= :startEpochMillis
          AND occurredAtEpochMillis < :endEpochMillis
        ORDER BY occurredAtEpochMillis DESC
        """
    )
    fun observeBetween(startEpochMillis: Long, endEpochMillis: Long): Flow<List<TransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: String)
}