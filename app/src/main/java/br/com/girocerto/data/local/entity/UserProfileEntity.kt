package br.com.girocerto.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import br.com.girocerto.domain.model.EmergencyChannel

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val displayName: String,
    val bikeModel: String,
    val dailyGoalCents: Long,
    val isDarkMode: Boolean,
    val emergencyContactName: String,
    val emergencyContactPhone: String,
    val emergencyChannel: EmergencyChannel
)