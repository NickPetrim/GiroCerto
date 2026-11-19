package br.com.girocerto.feature.forum

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import br.com.girocerto.R
import br.com.girocerto.core.designsystem.BigActionButton
import br.com.girocerto.core.designsystem.GiroColors
import br.com.girocerto.domain.model.ForumCategory
import br.com.girocerto.domain.model.ForumPost
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun ForumScreen(
    state: ForumUiState,
    onCategorySelected: (ForumCategory?) -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onToggleComposer: (Boolean) -> Unit,
    onDraftCategoryChange: (ForumCategory) -> Unit,
    onDraftTitleChange: (String) -> Unit,
    onDraftAreaChange: (String) -> Unit,
    onDraftDescriptionChange: (String) -> Unit,
    onPublishPost: () -> Unit,
    onToggleSupport: (String) -> Unit,
    onReportPost: (String) -> Unit,
    onBlockAuthor: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var postPendingReport by remember { mutableStateOf<ForumPost?>(null) }
    var authorPendingBlock by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = stringResource(R.string.forum_title),
            style = MaterialTheme.typography.headlineLarge,
            color = GiroColors.TextPrimary
        )

        BigActionButton(
            text = stringResource(
                if (state.isComposerVisible) R.string.entry_cancel else R.string.forum_btn_new_post
            ),
            onClick = { onToggleComposer(!state.isComposerVisible) },
            containerColor = if (state.isComposerVisible) GiroColors.SurfaceElevated else GiroColors.PrimaryYellow,
            contentColor = if (state.isComposerVisible) GiroColors.TextPrimary else GiroColors.TextOnBrightAccent,
            borderColor = if (state.isComposerVisible) GiroColors.BorderHigh else Color.Transparent,
            iconRes = if (state.isComposerVisible) null else R.drawable.ic_add,
            modifier = Modifier.fillMaxWidth()
        )

        if (state.isComposerVisible) {
            NewPostComposerCard(
                state = state,
                onDraftCategoryChange = onDraftCategoryChange,
                onDraftTitleChange = onDraftTitleChange,
                onDraftAreaChange = onDraftAreaChange,
                onDraftDescriptionChange = onDraftDescriptionChange,
                onPublishPost = onPublishPost
            )
        }

        OutlinedTextField(
            value = state.searchQuery,
            onValueChange = onSearchQueryChange,
            singleLine = true,
            placeholder = { Text(text = stringResource(R.string.forum_search_hint)) },
            leadingIcon = {
                Icon(
                    painter = painterResource(R.drawable.ic_search),
                    contentDescription = null,
                    tint = GiroColors.TextSecondary
                )
            },
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = GiroColors.SurfaceCard,
                unfocusedContainerColor = GiroColors.SurfaceCard,
                focusedBorderColor = GiroColors.TopBarNavy,
                unfocusedBorderColor = GiroColors.BorderHigh,
                focusedTextColor = GiroColors.TextPrimary,
                unfocusedTextColor = GiroColors.TextPrimary,
                focusedPlaceholderColor = GiroColors.TextSecondary,
                unfocusedPlaceholderColor = GiroColors.TextSecondary
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CategoryChipButton(
                label = stringResource(R.string.forum_filter_all),
                selected = state.selectedCategory == null,
                onClick = { onCategorySelected(null) }
            )
            ForumCategory.entries.forEach { category ->
                CategoryChipButton(
                    label = stringResource(forumCategoryLabelRes(category)),
                    selected = state.selectedCategory == category,
                    onClick = { onCategorySelected(category) }
                )
            }
        }

        if (state.posts.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = GiroColors.SurfaceCard,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, GiroColors.BorderHigh)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = stringResource(R.string.forum_empty_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = GiroColors.TextPrimary
                    )
                    Text(
                        text = stringResource(R.string.forum_empty_desc),
                        style = MaterialTheme.typography.bodyLarge,
                        color = GiroColors.TextSecondary
                    )
                }
            }
        } else {
            state.posts.forEach { post ->
                ForumPostCard(
                    post = post,
                    onToggleSupport = { onToggleSupport(post.id) },
                    onRequestReport = { postPendingReport = post },
                    onRequestBlock = { authorPendingBlock = post.authorName }
                )
            }
        }
    }

    postPendingReport?.let { target ->
        AlertDialog(
            onDismissRequest = { postPendingReport = null },
            containerColor = GiroColors.SurfaceCard,
            titleContentColor = GiroColors.TextPrimary,
            textContentColor = GiroColors.TextSecondary,
            title = { Text(text = stringResource(R.string.forum_report_confirm_title)) },
            text = { Text(text = stringResource(R.string.forum_report_confirm_desc)) },
            confirmButton = {
                BigActionButton(
                    text = stringResource(R.string.forum_btn_report),
                    onClick = {
                        onReportPost(target.id)
                        postPendingReport = null
                    },
                    containerColor = GiroColors.ExpenseCoral,
                    contentColor = GiroColors.TextOnBrightAccent,
                    minHeight = 64.dp
                )
            },
            dismissButton = {
                BigActionButton(
                    text = stringResource(R.string.entry_cancel),
                    onClick = { postPendingReport = null },
                    containerColor = GiroColors.SurfaceElevated,
                    contentColor = GiroColors.TextPrimary,
                    minHeight = 64.dp
                )
            }
        )
    }

    authorPendingBlock?.let { author ->
        AlertDialog(
            onDismissRequest = { authorPendingBlock = null },
            containerColor = GiroColors.SurfaceCard,
            titleContentColor = GiroColors.TextPrimary,
            textContentColor = GiroColors.TextSecondary,
            title = { Text(text = stringResource(R.string.forum_block_confirm_title, author)) },
            text = { Text(text = stringResource(R.string.forum_block_confirm_desc)) },
            confirmButton = {
                BigActionButton(
                    text = stringResource(R.string.forum_btn_block),
                    onClick = {
                        onBlockAuthor(author)
                        authorPendingBlock = null
                    },
                    containerColor = GiroColors.ExpenseCoral,
                    contentColor = GiroColors.TextOnBrightAccent,
                    minHeight = 64.dp
                )
            },
            dismissButton = {
                BigActionButton(
                    text = stringResource(R.string.entry_cancel),
                    onClick = { authorPendingBlock = null },
                    containerColor = GiroColors.SurfaceElevated,
                    contentColor = GiroColors.TextPrimary,
                    minHeight = 64.dp
                )
            }
        )
    }
}

@Composable
private fun NewPostComposerCard(
    state: ForumUiState,
    onDraftCategoryChange: (ForumCategory) -> Unit,
    onDraftTitleChange: (String) -> Unit,
    onDraftAreaChange: (String) -> Unit,
    onDraftDescriptionChange: (String) -> Unit,
    onPublishPost: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = GiroColors.SurfaceCard,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, GiroColors.TopBarNavy)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.forum_form_title),
                style = MaterialTheme.typography.titleLarge,
                color = GiroColors.InfoCyan
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ForumCategory.entries.forEach { category ->
                    CategoryChipButton(
                        label = stringResource(forumCategoryLabelRes(category)),
                        selected = state.draftCategory == category,
                        onClick = { onDraftCategoryChange(category) }
                    )
                }
            }

            OutlinedTextField(
                value = state.draftTitle,
                onValueChange = onDraftTitleChange,
                singleLine = true,
                label = { Text(stringResource(R.string.forum_field_title)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.draftArea,
                onValueChange = onDraftAreaChange,
                singleLine = true,
                label = { Text(stringResource(R.string.forum_field_area)) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = state.draftDescription,
                onValueChange = onDraftDescriptionChange,
                minLines = 3,
                label = { Text(stringResource(R.string.forum_field_desc)) },
                modifier = Modifier.fillMaxWidth()
            )

            if (state.showValidationError) {
                Text(
                    text = stringResource(R.string.forum_error_required),
                    style = MaterialTheme.typography.bodyLarge,
                    color = GiroColors.ExpenseText
                )
            }

            BigActionButton(
                text = stringResource(R.string.forum_btn_publish),
                onClick = onPublishPost,
                containerColor = GiroColors.GainGreen,
                contentColor = GiroColors.TextOnBrightAccent,
                iconRes = R.drawable.ic_check,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun CategoryChipButton(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.defaultMinSize(minWidth = 64.dp, minHeight = 64.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) GiroColors.TabSelectedBg else GiroColors.SurfaceCard,
            contentColor = if (selected) GiroColors.TextOnBrightAccent else GiroColors.TextPrimary
        ),
        border = BorderStroke(1.5.dp, GiroColors.BorderHigh)
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun ForumPostCard(
    post: ForumPost,
    onToggleSupport: () -> Unit,
    onRequestReport: () -> Unit,
    onRequestBlock: () -> Unit
) {
    val formatter = remember { DateTimeFormatter.ofPattern("dd/MM HH:mm") }
    val formattedDate = remember(post.createdAtEpochMillis) {
        Instant.ofEpochMilli(post.createdAtEpochMillis)
            .atZone(ZoneId.of("America/Sao_Paulo"))
            .format(formatter)
    }

    val (badgeBg, badgeFg) = when (post.category) {
        ForumCategory.BLITZ -> Pair(Color(0xFFFFC107), Color(0xFF000000))
        ForumCategory.ACCIDENT -> Pair(Color(0xFF990000), Color(0xFFFFFFFF))
        ForumCategory.SUPPORT_POINT -> Pair(Color(0xFF005C2E), Color(0xFFFFFFFF))
        ForumCategory.MECHANICS -> Pair(Color(0xFF0F2744), Color(0xFFFFFFFF))
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = GiroColors.SurfaceCard,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.5.dp, GiroColors.BorderHigh),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(color = badgeBg, shape = RoundedCornerShape(8.dp)) {
                    Text(
                        text = stringResource(forumCategoryLabelRes(post.category)),
                        color = badgeFg,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Text(
                    text = formattedDate,
                    style = MaterialTheme.typography.bodyMedium,
                    color = GiroColors.TextSecondary
                )
            }

            Text(
                text = post.title,
                style = MaterialTheme.typography.titleLarge,
                color = GiroColors.TextPrimary
            )
            Text(
                text = "${post.locationArea} · por ${post.authorName}",
                style = MaterialTheme.typography.bodyMedium,
                color = GiroColors.InfoCyan,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = post.description,
                style = MaterialTheme.typography.bodyLarge,
                color = GiroColors.TextSecondary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onToggleSupport,
                    modifier = Modifier
                        .weight(1.2f)
                        .defaultMinSize(minHeight = 64.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (post.isSupportedByMe) GiroColors.GainGreen else GiroColors.SurfaceElevated,
                        contentColor = if (post.isSupportedByMe) GiroColors.TextOnBrightAccent else GiroColors.TextPrimary
                    ),
                    border = BorderStroke(1.5.dp, GiroColors.BorderHigh)
                ) {
                    Text(
                        text = stringResource(
                            if (post.isSupportedByMe) R.string.forum_btn_supported else R.string.forum_btn_support,
                            post.supportCount
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onRequestReport,
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 64.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GiroColors.SurfaceElevated,
                        contentColor = GiroColors.ExpenseText
                    ),
                    border = BorderStroke(1.5.dp, GiroColors.BorderHigh)
                ) {
                    Text(
                        text = stringResource(R.string.forum_btn_report),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onRequestBlock,
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 64.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GiroColors.SurfaceElevated,
                        contentColor = GiroColors.TextSecondary
                    ),
                    border = BorderStroke(1.5.dp, GiroColors.BorderHigh)
                ) {
                    Text(
                        text = stringResource(R.string.forum_btn_block),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

fun forumCategoryLabelRes(category: ForumCategory): Int = when (category) {
    ForumCategory.BLITZ -> R.string.forum_cat_blitz
    ForumCategory.ACCIDENT -> R.string.forum_cat_accident
    ForumCategory.SUPPORT_POINT -> R.string.forum_cat_support
    ForumCategory.MECHANICS -> R.string.forum_cat_mechanics
}