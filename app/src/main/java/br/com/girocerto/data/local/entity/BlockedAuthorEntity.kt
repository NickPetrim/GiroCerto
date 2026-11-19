package br.com.girocerto.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "blocked_authors")
data class BlockedAuthorEntity(
    @PrimaryKey
    val authorName: String,
    val blockedAtEpochMillis: Long
)