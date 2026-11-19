package br.com.girocerto.domain.repository

import br.com.girocerto.domain.model.EmergencyChannel
import br.com.girocerto.domain.model.ForumCategory
import br.com.girocerto.domain.model.ForumPost
import br.com.girocerto.domain.model.UserProfile
import kotlinx.coroutines.flow.Flow

interface UserProfileRepository {
    fun observeProfile(): Flow<UserProfile>
    suspend fun setDarkMode(enabled: Boolean)
    suspend fun updateProfile(
        displayName: String,
        bikeModel: String,
        dailyGoalCents: Long,
        emergencyContactName: String,
        emergencyContactPhone: String,
        emergencyChannel: EmergencyChannel
    )
}

interface ForumRepository {
    fun observeVisiblePosts(): Flow<List<ForumPost>>
    suspend fun createPost(
        authorName: String,
        category: ForumCategory,
        title: String,
        description: String,
        locationArea: String
    )
    suspend fun toggleSupport(postId: String)
    suspend fun reportPost(postId: String)
    suspend fun blockAuthor(authorName: String)
}