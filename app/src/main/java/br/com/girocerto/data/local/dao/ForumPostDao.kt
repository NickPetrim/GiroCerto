package br.com.girocerto.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import br.com.girocerto.data.local.entity.BlockedAuthorEntity
import br.com.girocerto.data.local.entity.ForumPostEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ForumPostDao {

    @Query(
        """
        SELECT * FROM forum_posts
        WHERE isReported = 0
          AND authorName NOT IN (SELECT authorName FROM blocked_authors)
        ORDER BY createdAtEpochMillis DESC
        """
    )
    fun observeVisiblePosts(): Flow<List<ForumPostEntity>>

    @Query("SELECT * FROM forum_posts WHERE id = :postId LIMIT 1")
    suspend fun getById(postId: String): ForumPostEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: ForumPostEntity)

    @Query("UPDATE forum_posts SET isSupportedByMe = :supported, supportCount = :count WHERE id = :postId")
    suspend fun updateSupport(postId: String, supported: Boolean, count: Int)

    @Query("UPDATE forum_posts SET isReported = 1 WHERE id = :postId")
    suspend fun markReported(postId: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun blockAuthor(blockedAuthor: BlockedAuthorEntity)
}