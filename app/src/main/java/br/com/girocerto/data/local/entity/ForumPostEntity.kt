package br.com.girocerto.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import br.com.girocerto.domain.model.ForumCategory

@Entity(tableName = "forum_posts")
data class ForumPostEntity(
    @PrimaryKey
    val id: String,
    val authorName: String,
    val category: ForumCategory,
    val title: String,
    val description: String,
    val locationArea: String,
    val supportCount: Int,
    val isSupportedByMe: Boolean,
    val isReported: Boolean,
    val createdAtEpochMillis: Long
)