package br.com.girocerto.feature.forum

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.com.girocerto.domain.model.ForumCategory
import br.com.girocerto.domain.model.ForumPost
import br.com.girocerto.domain.repository.ForumRepository
import br.com.girocerto.domain.repository.UserProfileRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ForumUiState(
    val selectedCategory: ForumCategory? = null,
    val searchQuery: String = "",
    val posts: List<ForumPost> = emptyList(),
    val isComposerVisible: Boolean = false,
    val draftCategory: ForumCategory = ForumCategory.BLITZ,
    val draftTitle: String = "",
    val draftArea: String = "",
    val draftDescription: String = "",
    val showValidationError: Boolean = false
)

sealed interface ForumEvent {
    data object PostCreated : ForumEvent
    data object PostReported : ForumEvent
    data object AuthorBlocked : ForumEvent
}

class ForumViewModel(
    private val forumRepository: ForumRepository,
    private val profileRepository: UserProfileRepository
) : ViewModel() {

    private val filterCategory = MutableStateFlow<ForumCategory?>(null)
    private val searchQuery = MutableStateFlow("")
    private val draftState = MutableStateFlow(ForumUiState())

    private val _events = Channel<ForumEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    val uiState: StateFlow<ForumUiState> = combine(
        forumRepository.observeVisiblePosts(),
        filterCategory,
        searchQuery,
        draftState
    ) { allPosts, category, query, draft ->
        val byCategory = if (category == null) allPosts else allPosts.filter { it.category == category }
        val filtered = if (query.isBlank()) {
            byCategory
        } else {
            val q = query.trim().lowercase()
            byCategory.filter {
                it.title.lowercase().contains(q) ||
                        it.description.lowercase().contains(q) ||
                        it.locationArea.lowercase().contains(q)
            }
        }
        draft.copy(
            selectedCategory = category,
            searchQuery = query,
            posts = filtered
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000L),
        initialValue = ForumUiState()
    )

    fun onCategorySelected(category: ForumCategory?) {
        filterCategory.value = category
    }

    fun onSearchQueryChange(query: String) {
        searchQuery.value = query
    }

    fun onToggleComposer(visible: Boolean) {
        draftState.update {
            it.copy(
                isComposerVisible = visible,
                showValidationError = false
            )
        }
    }

    fun onDraftCategoryChange(category: ForumCategory) {
        draftState.update { it.copy(draftCategory = category) }
    }

    fun onDraftTitleChange(value: String) {
        draftState.update { it.copy(draftTitle = value, showValidationError = false) }
    }

    fun onDraftAreaChange(value: String) {
        draftState.update { it.copy(draftArea = value) }
    }

    fun onDraftDescriptionChange(value: String) {
        draftState.update { it.copy(draftDescription = value, showValidationError = false) }
    }

    fun onPublishPost() {
        val current = draftState.value
        if (current.draftTitle.isBlank() || current.draftDescription.isBlank()) {
            draftState.update { it.copy(showValidationError = true) }
            return
        }
        viewModelScope.launch {
            val authorName = profileRepository.observeProfile().first().displayName
            forumRepository.createPost(
                authorName = authorName,
                category = current.draftCategory,
                title = current.draftTitle,
                description = current.draftDescription,
                locationArea = current.draftArea
            )
            draftState.value = ForumUiState()
            _events.send(ForumEvent.PostCreated)
        }
    }

    fun onToggleSupport(postId: String) {
        viewModelScope.launch {
            forumRepository.toggleSupport(postId)
        }
    }

    fun onReportPost(postId: String) {
        viewModelScope.launch {
            forumRepository.reportPost(postId)
            _events.send(ForumEvent.PostReported)
        }
    }

    fun onBlockAuthor(authorName: String) {
        viewModelScope.launch {
            forumRepository.blockAuthor(authorName)
            _events.send(ForumEvent.AuthorBlocked)
        }
    }
}