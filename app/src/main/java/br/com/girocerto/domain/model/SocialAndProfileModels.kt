package br.com.girocerto.domain.model

enum class EmergencyChannel {
    WHATSAPP,
    SMS,
    CALL
}

data class UserProfile(
    val displayName: String = "Motoca SP",
    val bikeModel: String = "Moto Urbana",
    val dailyGoalCents: Long = HomeSummary.DEFAULT_DAILY_GOAL_CENTS,
    val isDarkMode: Boolean = false,
    val emergencyContactName: String = "",
    val emergencyContactPhone: String = "",
    val emergencyChannel: EmergencyChannel = EmergencyChannel.WHATSAPP
)

enum class ForumCategory {
    BLITZ,
    ACCIDENT,
    SUPPORT_POINT,
    MECHANICS
}

data class ForumPost(
    val id: String,
    val authorName: String,
    val category: ForumCategory,
    val title: String,
    val description: String,
    val locationArea: String,
    val supportCount: Int,
    val isSupportedByMe: Boolean,
    val createdAtEpochMillis: Long
)