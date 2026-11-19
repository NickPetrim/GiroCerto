package br.com.girocerto.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "work_sessions")
data class WorkSessionEntity(
    @PrimaryKey
    val dateKey: String,
    val startedAtEpochMillis: Long
)