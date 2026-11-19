package br.com.girocerto.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import br.com.girocerto.domain.model.ExpenseCategory
import br.com.girocerto.domain.model.Platform
import br.com.girocerto.domain.model.TransactionType

@Entity(
    tableName = "transactions",
    indices = [Index(value = ["occurredAtEpochMillis"])]
)
data class TransactionEntity(
    @PrimaryKey
    val id: String,
    val type: TransactionType,
    val amountCents: Long,
    val platform: Platform?,
    val expenseCategory: ExpenseCategory?,
    val occurredAtEpochMillis: Long,
    val syncPending: Boolean
)