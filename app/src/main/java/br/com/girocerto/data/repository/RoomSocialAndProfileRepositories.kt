package br.com.girocerto.data.repository

import br.com.girocerto.core.util.ClockProvider
import br.com.girocerto.data.local.dao.ForumPostDao
import br.com.girocerto.data.local.dao.UserProfileDao
import br.com.girocerto.data.local.entity.BlockedAuthorEntity
import br.com.girocerto.data.local.entity.ForumPostEntity
import br.com.girocerto.data.local.entity.UserProfileEntity
import br.com.girocerto.domain.model.EmergencyChannel
import br.com.girocerto.domain.model.ForumCategory
import br.com.girocerto.domain.model.ForumPost
import br.com.girocerto.domain.model.UserProfile
import br.com.girocerto.domain.repository.ForumRepository
import br.com.girocerto.domain.repository.UserProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.util.UUID

class RoomUserProfileRepository(
    private val userProfileDao: UserProfileDao
) : UserProfileRepository {

    override fun observeProfile(): Flow<UserProfile> {
        return userProfileDao.observeProfile().map { entity ->
            entity?.toDomain() ?: UserProfile()
        }
    }

    override suspend fun setDarkMode(enabled: Boolean) {
        val current = userProfileDao.getProfileOnce()
        if (current == null) {
            userProfileDao.upsert(UserProfile().copy(isDarkMode = enabled).toEntity())
        } else {
            userProfileDao.updateDarkMode(enabled)
        }
    }

    override suspend fun updateProfile(
        displayName: String,
        bikeModel: String,
        dailyGoalCents: Long,
        emergencyContactName: String,
        emergencyContactPhone: String,
        emergencyChannel: EmergencyChannel
    ) {
        val current = userProfileDao.getProfileOnce()?.toDomain() ?: UserProfile()
        val updated = current.copy(
            displayName = displayName.trim().ifBlank { "Motoca SP" },
            bikeModel = bikeModel.trim().ifBlank { "Moto Urbana" },
            dailyGoalCents = dailyGoalCents.coerceAtLeast(100L),
            emergencyContactName = emergencyContactName.trim(),
            emergencyContactPhone = emergencyContactPhone.trim(),
            emergencyChannel = emergencyChannel
        )
        userProfileDao.upsert(updated.toEntity())
    }

    private fun UserProfileEntity.toDomain() = UserProfile(
        displayName = displayName,
        bikeModel = bikeModel,
        dailyGoalCents = dailyGoalCents,
        isDarkMode = isDarkMode,
        emergencyContactName = emergencyContactName,
        emergencyContactPhone = emergencyContactPhone,
        emergencyChannel = emergencyChannel
    )

    private fun UserProfile.toEntity() = UserProfileEntity(
        id = 1,
        displayName = displayName,
        bikeModel = bikeModel,
        dailyGoalCents = dailyGoalCents,
        isDarkMode = isDarkMode,
        emergencyContactName = emergencyContactName,
        emergencyContactPhone = emergencyContactPhone,
        emergencyChannel = emergencyChannel
    )
}

class DefaultInMemoryUserProfileRepository : UserProfileRepository {
    private val profileFlow = MutableStateFlow(UserProfile())
    override fun observeProfile(): Flow<UserProfile> = profileFlow
    override suspend fun setDarkMode(enabled: Boolean) {
        profileFlow.value = profileFlow.value.copy(isDarkMode = enabled)
    }
    override suspend fun updateProfile(
        displayName: String,
        bikeModel: String,
        dailyGoalCents: Long,
        emergencyContactName: String,
        emergencyContactPhone: String,
        emergencyChannel: EmergencyChannel
    ) {
        profileFlow.value = profileFlow.value.copy(
            displayName = displayName,
            bikeModel = bikeModel,
            dailyGoalCents = dailyGoalCents,
            emergencyContactName = emergencyContactName,
            emergencyContactPhone = emergencyContactPhone,
            emergencyChannel = emergencyChannel
        )
    }
}

class RoomForumRepository(
    private val forumPostDao: ForumPostDao,
    private val clockProvider: ClockProvider
) : ForumRepository {

    override fun observeVisiblePosts(): Flow<List<ForumPost>> {
        return forumPostDao.observeVisiblePosts().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun createPost(
        authorName: String,
        category: ForumCategory,
        title: String,
        description: String,
        locationArea: String
    ) {
        val cleanTitle = title.trim()
        val cleanDesc = description.trim()
        require(cleanTitle.isNotEmpty() && cleanDesc.isNotEmpty()) {
            "Título e descrição são obrigatórios."
        }
        forumPostDao.insert(
            ForumPostEntity(
                id = UUID.randomUUID().toString(),
                authorName = authorName.trim().ifBlank { "Motoca SP" },
                category = category,
                title = cleanTitle,
                description = cleanDesc,
                locationArea = locationArea.trim().ifBlank { "São Paulo - Geral" },
                supportCount = 1,
                isSupportedByMe = true,
                isReported = false,
                createdAtEpochMillis = clockProvider.nowEpochMillis()
            )
        )
    }

    override suspend fun toggleSupport(postId: String) {
        val post = forumPostDao.getById(postId) ?: return
        val nextSupported = !post.isSupportedByMe
        val nextCount = if (nextSupported) post.supportCount + 1 else (post.supportCount - 1).coerceAtLeast(0)
        forumPostDao.updateSupport(postId, nextSupported, nextCount)
    }

    override suspend fun reportPost(postId: String) {
        forumPostDao.markReported(postId)
    }

    override suspend fun blockAuthor(authorName: String) {
        forumPostDao.blockAuthor(
            BlockedAuthorEntity(
                authorName = authorName,
                blockedAtEpochMillis = clockProvider.nowEpochMillis()
            )
        )
    }

    private fun ForumPostEntity.toDomain() = ForumPost(
        id = id,
        authorName = authorName,
        category = category,
        title = title,
        description = description,
        locationArea = locationArea,
        supportCount = supportCount,
        isSupportedByMe = isSupportedByMe,
        createdAtEpochMillis = createdAtEpochMillis
    )
}