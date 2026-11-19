package br.com.girocerto

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import br.com.girocerto.core.designsystem.GiroCertoTheme
import br.com.girocerto.domain.model.TransactionType
import br.com.girocerto.domain.validator.TransactionAmountValidator
import br.com.girocerto.feature.entry.QuickEntryEvent
import br.com.girocerto.feature.entry.QuickEntryScreen
import br.com.girocerto.feature.entry.QuickEntryViewModel
import br.com.girocerto.feature.home.HomeScreen
import br.com.girocerto.feature.home.HomeViewModel
import kotlinx.coroutines.launch
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QuickEntryAndUndoFlowTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun fluxoRegistroComValidacaoZeroConfirmacaoEDesfazer() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val clock = FakeUiClockProvider()
        val repo = FakeUiTransactionRepository(clock)
        val homeViewModel = HomeViewModel(repo, clock)
        val entryViewModel = QuickEntryViewModel(
            savedStateHandle = SavedStateHandle(mapOf("type" to TransactionType.EARNING.name)),
            repository = repo,
            validator = TransactionAmountValidator()
        )

        composeRule.setContent {
            val snackbarHostState = remember { SnackbarHostState() }
            val scope = rememberCoroutineScope()
            var showEntry by remember { mutableStateOf(false) }

            LaunchedEffect(entryViewModel) {
                entryViewModel.events.collect { event ->
                    if (event is QuickEntryEvent.Saved) {
                        showEntry = false
                        scope.launch {
                            val result = snackbarHostState.showSnackbar(
                                message = "Ganho salvo",
                                actionLabel = context.getString(R.string.entry_undo),
                                duration = SnackbarDuration.Long
                            )
                            if (result == SnackbarResult.ActionPerformed) {
                                homeViewModel.undoTransaction(event.transaction.id)
                            }
                        }
                    }
                }
            }

            GiroCertoTheme {
                Scaffold(
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { padding ->
                    Column(modifier = Modifier.padding(padding)) {
                        if (!showEntry) {
                            val homeState by homeViewModel.uiState.collectAsStateWithLifecycle()
                            HomeScreen(
                                summary = homeState,
                                onAddEarningClick = { showEntry = true },
                                onAddExpenseClick = {}
                            )
                        } else {
                            val entryState by entryViewModel.uiState.collectAsStateWithLifecycle()
                            QuickEntryScreen(
                                state = entryState,
                                onDigitPressed = entryViewModel::onDigitPressed,
                                onBackspacePressed = entryViewModel::onBackspacePressed,
                                onClearPressed = entryViewModel::onClearPressed,
                                onProceedFromAmount = entryViewModel::onProceedFromAmount,
                                onPlatformSelected = entryViewModel::onPlatformSelected,
                                onCategorySelected = entryViewModel::onCategorySelected,
                                onBackFromSelection = entryViewModel::onBackFromSelection,
                                onCorrectFromConfirmation = entryViewModel::onCorrectFromConfirmation,
                                onConfirmSave = entryViewModel::onConfirmSave,
                                onCancelEntry = { showEntry = false }
                            )
                        }
                    }
                }
            }
        }

        composeRule.onNodeWithText("R$ 0,00").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.home_btn_add_earning)).performClick()

        composeRule.onNodeWithText(context.getString(R.string.entry_next)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.entry_error_zero)).assertIsDisplayed()

        composeRule.onNodeWithText("4").performClick()
        composeRule.onNodeWithText("5").performClick()
        composeRule.onNodeWithText("5").performClick()
        composeRule.onNodeWithText("0").performClick()
        composeRule.onNodeWithText("R$ 45,50").assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.entry_next)).performClick()

        composeRule.onNodeWithText(context.getString(R.string.platform_ifood)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.entry_confirm)).assertIsDisplayed()
        composeRule.onNodeWithText(context.getString(R.string.entry_correct)).assertIsDisplayed()

        composeRule.onNodeWithText(context.getString(R.string.entry_confirm)).performClick()
        val undoText = context.getString(R.string.entry_undo)
        composeRule.waitUntil(timeoutMillis = 3_000L) {
            composeRule.onAllNodesWithText(undoText).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("R$ 45,50").assertIsDisplayed()

        composeRule.onNodeWithText(undoText).performClick()
        composeRule.waitUntil(timeoutMillis = 3_000L) {
            composeRule.onAllNodesWithText("R$ 0,00").fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("R$ 0,00").assertIsDisplayed()
    }
}