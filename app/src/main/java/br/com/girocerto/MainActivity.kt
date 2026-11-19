package br.com.girocerto

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import br.com.girocerto.core.designsystem.BigActionButton
import br.com.girocerto.core.designsystem.GiroCertoTheme
import br.com.girocerto.core.designsystem.GiroColors
import br.com.girocerto.core.designsystem.GiroTopBar
import br.com.girocerto.core.util.BrlFormatter
import br.com.girocerto.domain.model.ForumCategory
import br.com.girocerto.domain.model.Transaction
import br.com.girocerto.domain.model.TransactionType
import br.com.girocerto.feature.calculator.RideViabilityScreen
import br.com.girocerto.feature.calculator.RideViabilityViewModel
import br.com.girocerto.feature.earnings.EarningsScreen
import br.com.girocerto.feature.earnings.EarningsViewModel
import br.com.girocerto.feature.entry.QuickEntryEvent
import br.com.girocerto.feature.entry.QuickEntryScreen
import br.com.girocerto.feature.entry.QuickEntryViewModel
import br.com.girocerto.feature.entry.categoryLabelRes
import br.com.girocerto.feature.entry.platformLabelRes
import br.com.girocerto.feature.forum.ForumEvent
import br.com.girocerto.feature.forum.ForumScreen
import br.com.girocerto.feature.forum.ForumViewModel
import br.com.girocerto.feature.forum.forumCategoryLabelRes
import br.com.girocerto.feature.history.HistoryScreen
import br.com.girocerto.feature.history.HistoryViewModel
import br.com.girocerto.feature.home.HomeScreen
import br.com.girocerto.feature.home.HomeViewModel
import br.com.girocerto.feature.hud.HudScreen
import br.com.girocerto.feature.hud.HudViewModel
import br.com.girocerto.feature.profile.ProfileEvent
import br.com.girocerto.feature.profile.ProfileScreen
import br.com.girocerto.feature.profile.ProfileViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.seconds

private object Routes {
    const val OVERVIEW = "overview"
    const val FORUM = "forum"
    const val EARNINGS = "earnings"
    const val PROFILE = "profile"
    const val HISTORY = "history"
    const val CALCULATOR = "calculator"
    const val QUICK_ENTRY = "quick_entry/{type}"
    fun quickEntry(type: TransactionType) = "quick_entry/${type.name}"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.rgb(15, 39, 68)),
            navigationBarStyle = SystemBarStyle.dark(Color.rgb(15, 39, 68))
        )
        val viewModelFactory = (application as GiroCertoApp).container.viewModelFactory
        setContent {
            GiroCertoAppRoot(viewModelFactory = viewModelFactory)
        }
    }
}

@Composable
fun GiroCertoAppRoot(
    viewModelFactory: ViewModelProvider.Factory
) {
    val homeViewModel: HomeViewModel = viewModel(factory = viewModelFactory)
    val profileViewModel: ProfileViewModel = viewModel(factory = viewModelFactory)
    val forumViewModel: ForumViewModel = viewModel(factory = viewModelFactory)
    val hudViewModel: HudViewModel = viewModel(factory = viewModelFactory)

    val profileState by profileViewModel.uiState.collectAsStateWithLifecycle()
    val forumState by forumViewModel.uiState.collectAsStateWithLifecycle()
    val homeState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val hudState by hudViewModel.uiState.collectAsStateWithLifecycle()

    val navController = rememberNavController()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val resources = LocalResources.current

    var askedLocationPromptOnce by rememberSaveable { mutableStateOf(false) }
    var showGpsRationaleDialog by rememberSaveable { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val granted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                result[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hudViewModel.onLocationPermissionResult(granted)
    }

    LaunchedEffect(Unit) {
        val alreadyGranted = hasLocationPermission(context)
        hudViewModel.onLocationPermissionResult(alreadyGranted)
        if (!alreadyGranted && !askedLocationPromptOnce) {
            askedLocationPromptOnce = true
            showGpsRationaleDialog = true
        }
    }

    val isHudActive = hudState.isHudActive
    val view = LocalView.current
    DisposableEffect(isHudActive) {
        view.keepScreenOn = isHudActive
        onDispose { view.keepScreenOn = false }
    }

    val latestStreetAlert = forumState.posts.firstOrNull {
        it.category == ForumCategory.BLITZ || it.category == ForumCategory.ACCIDENT
    } ?: forumState.posts.firstOrNull()

    LaunchedEffect(isHudActive, latestStreetAlert?.id, hudState.isVoiceCopilotEnabled) {
        val alert = latestStreetAlert
        val speech = if (alert != null) {
            val catLabel = resources.getString(forumCategoryLabelRes(alert.category))
            resources.getString(R.string.hud_voice_alert_spoken, catLabel, alert.locationArea, alert.title)
        } else {
            null
        }
        hudViewModel.onLatestAlertChanged(alert, speech)
    }

    LaunchedEffect(forumViewModel) {
        forumViewModel.events.collect { event ->
            val msgRes = when (event) {
                ForumEvent.PostCreated -> R.string.forum_post_created_msg
                ForumEvent.PostReported -> R.string.forum_post_reported_msg
                ForumEvent.AuthorBlocked -> R.string.forum_author_blocked_msg
            }
            snackbarHostState.showSnackbar(
                message = resources.getString(msgRes),
                duration = SnackbarDuration.Short
            )
        }
    }

    LaunchedEffect(profileViewModel) {
        profileViewModel.events.collect { event ->
            if (event is ProfileEvent.Saved) {
                snackbarHostState.showSnackbar(
                    message = resources.getString(R.string.profile_saved_msg),
                    duration = SnackbarDuration.Short
                )
            }
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val mainTabs = setOf(Routes.OVERVIEW, Routes.FORUM, Routes.EARNINGS, Routes.PROFILE)
    val showChrome = !isHudActive && (currentRoute in mainTabs || currentRoute == Routes.HISTORY)

    GiroCertoTheme(
        darkTheme = profileState.isDarkMode,
        hudMode = isHudActive
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding(),
            containerColor = if (isHudActive) GiroColors.PureBlack else GiroColors.BackgroundDark,
            topBar = {
                if (!isHudActive) {
                    GiroTopBar()
                }
            },
            snackbarHost = {
                SnackbarHost(hostState = snackbarHostState) { data ->
                    HighContrastUndoSnackbar(data = data)
                }
            },
            bottomBar = {
                if (showChrome) {
                    GiroBottomNavigation(
                        currentRoute = currentRoute,
                        onSelectTab = { targetRoute ->
                            navController.navigate(targetRoute) {
                                launchSingleTop = true
                                popUpTo(Routes.OVERVIEW) { inclusive = false }
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isHudActive) {
                    HudScreen(
                        hudState = hudState,
                        homeSummary = homeState,
                        latestStreetAlert = latestStreetAlert,
                        onToggleVoiceCopilot = {
                            hudViewModel.onToggleVoiceCopilot(
                                resources.getString(R.string.hud_voice_enabled_spoken)
                            )
                        },
                        onExitHud = hudViewModel::onExitHud
                    )
                } else {
                    NavHost(
                        navController = navController,
                        startDestination = Routes.EARNINGS
                    ) {
                        composable(Routes.OVERVIEW) {
                            HomeScreen(
                                summary = homeState,
                                onAddEarningClick = {
                                    navController.navigate(Routes.quickEntry(TransactionType.EARNING))
                                },
                                onAddExpenseClick = {
                                    navController.navigate(Routes.quickEntry(TransactionType.EXPENSE))
                                },
                                isAutoGpsEnabled = hudState.isAutoGpsEnabled,
                                onEnableAutoGpsClick = { showGpsRationaleDialog = true },
                                onOpenCalculatorClick = {
                                    navController.navigate(Routes.CALCULATOR) { launchSingleTop = true }
                                },
                                onEnterHudClick = hudViewModel::onEnterManualHud,
                                onOpenHistoryClick = {
                                    navController.navigate(Routes.HISTORY) { launchSingleTop = true }
                                }
                            )
                        }

                        composable(Routes.FORUM) {
                            ForumScreen(
                                state = forumState,
                                onCategorySelected = forumViewModel::onCategorySelected,
                                onSearchQueryChange = forumViewModel::onSearchQueryChange,
                                onToggleComposer = forumViewModel::onToggleComposer,
                                onDraftCategoryChange = forumViewModel::onDraftCategoryChange,
                                onDraftTitleChange = forumViewModel::onDraftTitleChange,
                                onDraftAreaChange = forumViewModel::onDraftAreaChange,
                                onDraftDescriptionChange = forumViewModel::onDraftDescriptionChange,
                                onPublishPost = forumViewModel::onPublishPost,
                                onToggleSupport = forumViewModel::onToggleSupport,
                                onReportPost = forumViewModel::onReportPost,
                                onBlockAuthor = forumViewModel::onBlockAuthor
                            )
                        }

                        composable(Routes.EARNINGS) {
                            val earningsViewModel: EarningsViewModel = viewModel(factory = viewModelFactory)
                            val earningsState by earningsViewModel.uiState.collectAsStateWithLifecycle()
                            EarningsScreen(
                                state = earningsState,
                                onSearchQueryChange = earningsViewModel::onSearchQueryChange,
                                onAddRideClick = {
                                    navController.navigate(Routes.quickEntry(TransactionType.EARNING))
                                },
                                onAddExpenseClick = {
                                    navController.navigate(Routes.quickEntry(TransactionType.EXPENSE))
                                },
                                onOpenHistoryClick = {
                                    navController.navigate(Routes.HISTORY) { launchSingleTop = true }
                                }
                            )
                        }

                        composable(Routes.PROFILE) {
                            ProfileScreen(
                                state = profileState,
                                onDisplayNameChange = profileViewModel::onDisplayNameChange,
                                onBikeModelChange = profileViewModel::onBikeModelChange,
                                onDailyGoalChange = profileViewModel::onDailyGoalChange,
                                onToggleDarkMode = profileViewModel::onToggleDarkMode,
                                onSaveProfile = profileViewModel::onSaveProfile,
                                onOpenHudClick = hudViewModel::onEnterManualHud
                            )
                        }

                        composable(Routes.HISTORY) {
                            val historyViewModel: HistoryViewModel = viewModel(factory = viewModelFactory)
                            val historyState by historyViewModel.uiState.collectAsStateWithLifecycle()
                            HistoryScreen(
                                state = historyState,
                                onTabSelected = historyViewModel::onTabSelected,
                                onGoHomeClick = {
                                    navController.navigate(Routes.OVERVIEW) {
                                        launchSingleTop = true
                                        popUpTo(Routes.OVERVIEW)
                                    }
                                }
                            )
                        }

                        composable(Routes.CALCULATOR) {
                            val calcViewModel: RideViabilityViewModel = viewModel(factory = viewModelFactory)
                            val calcState by calcViewModel.uiState.collectAsStateWithLifecycle()
                            RideViabilityScreen(
                                state = calcState,
                                onOfferChange = calcViewModel::onOfferChange,
                                onDistanceChange = calcViewModel::onDistanceChange,
                                onDurationChange = calcViewModel::onDurationChange,
                                onCostPerKmChange = calcViewModel::onCostPerKmChange,
                                onEvaluate = calcViewModel::onEvaluate,
                                onClear = calcViewModel::onClear,
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable(
                            route = Routes.QUICK_ENTRY,
                            arguments = listOf(navArgument("type") { type = NavType.StringType })
                        ) {
                            val entryViewModel: QuickEntryViewModel = viewModel(factory = viewModelFactory)
                            val entryState by entryViewModel.uiState.collectAsStateWithLifecycle()

                            LaunchedEffect(entryViewModel) {
                                entryViewModel.events.collect { event ->
                                    when (event) {
                                        is QuickEntryEvent.Saved -> {
                                            navController.popBackStack()
                                            scope.launch {
                                                showUndoSnackbarFor5Seconds(
                                                    snackbarHostState = snackbarHostState,
                                                    transaction = event.transaction,
                                                    resources = resources,
                                                    onUndo = {
                                                        homeViewModel.undoTransaction(event.transaction.id)
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }

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
                                onCancelEntry = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }

        if (showGpsRationaleDialog) {
            AlertDialog(
                onDismissRequest = { showGpsRationaleDialog = false },
                containerColor = GiroColors.SurfaceCard,
                titleContentColor = GiroColors.TextPrimary,
                textContentColor = GiroColors.TextSecondary,
                title = { Text(text = stringResource(R.string.gps_perm_title)) },
                text = { Text(text = stringResource(R.string.gps_perm_desc)) },
                confirmButton = {
                    BigActionButton(
                        text = stringResource(R.string.gps_perm_allow),
                        onClick = {
                            showGpsRationaleDialog = false
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        },
                        containerColor = GiroColors.GainGreen,
                        contentColor = GiroColors.TextOnBrightAccent,
                        minHeight = 64.dp
                    )
                },
                dismissButton = {
                    BigActionButton(
                        text = stringResource(R.string.gps_perm_later),
                        onClick = {
                            showGpsRationaleDialog = false
                            hudViewModel.onLocationPermissionResult(false)
                        },
                        containerColor = GiroColors.SurfaceElevated,
                        contentColor = GiroColors.TextPrimary,
                        minHeight = 64.dp
                    )
                }
            )
        }
    }
}

private fun hasLocationPermission(context: Context): Boolean {
    val fine = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    val coarse = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED
    return fine || coarse
}

private suspend fun showUndoSnackbarFor5Seconds(
    snackbarHostState: SnackbarHostState,
    transaction: Transaction,
    resources: Resources,
    onUndo: () -> Unit
) {
    val label = if (transaction.type == TransactionType.EARNING) {
        transaction.platform?.let { resources.getString(platformLabelRes(it)) }.orEmpty()
    } else {
        transaction.expenseCategory?.let { resources.getString(categoryLabelRes(it)) }.orEmpty()
    }
    val formattedValue = BrlFormatter.format(transaction.amountCents)
    val message = if (transaction.type == TransactionType.EARNING) {
        resources.getString(R.string.entry_saved_earning_msg, formattedValue, label)
    } else {
        resources.getString(R.string.entry_saved_expense_msg, formattedValue, label)
    }

    snackbarHostState.currentSnackbarData?.dismiss()

    coroutineScope {
        val autoDismissJob = launch {
            delay(5.seconds)
            snackbarHostState.currentSnackbarData?.dismiss()
        }
        val result = snackbarHostState.showSnackbar(
            message = message,
            actionLabel = resources.getString(R.string.entry_undo),
            duration = SnackbarDuration.Indefinite
        )
        autoDismissJob.cancel()
        if (result == SnackbarResult.ActionPerformed) {
            onUndo()
            snackbarHostState.showSnackbar(
                message = resources.getString(R.string.entry_undone_msg),
                duration = SnackbarDuration.Short
            )
        }
    }
}

@Composable
private fun HighContrastUndoSnackbar(data: SnackbarData) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        color = GiroColors.SurfaceCard,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(2.dp, GiroColors.PrimaryYellow),
        shadowElevation = 6.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = data.visuals.message,
                style = MaterialTheme.typography.bodyLarge,
                color = GiroColors.TextPrimary,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            )
            val actionLabel = data.visuals.actionLabel
            if (actionLabel != null) {
                Button(
                    onClick = { data.performAction() },
                    modifier = Modifier.defaultMinSize(minWidth = 64.dp, minHeight = 64.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GiroColors.PrimaryYellow,
                        contentColor = GiroColors.TextOnBrightAccent
                    )
                ) {
                    Text(
                        text = actionLabel,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun GiroBottomNavigation(
    currentRoute: String?,
    onSelectTab: (String) -> Unit
) {
    val tabs = listOf(
        Triple(Routes.OVERVIEW, R.string.nav_overview, Pair(R.drawable.ic_home, R.string.nav_cd_overview)),
        Triple(Routes.FORUM, R.string.nav_forum, Pair(R.drawable.ic_forum, R.string.nav_cd_forum)),
        Triple(Routes.EARNINGS, R.string.nav_earnings, Pair(R.drawable.ic_wallet, R.string.nav_cd_earnings)),
        Triple(Routes.PROFILE, R.string.nav_profile, Pair(R.drawable.ic_person, R.string.nav_cd_profile))
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = GiroColors.TopBarNavy
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            tabs.forEach { (route, labelRes, iconsAndCd) ->
                val selected = currentRoute == route
                val contentDesc = stringResource(iconsAndCd.second)
                val tint = if (selected) GiroColors.PrimaryYellow else GiroColors.TextMutedOnNavy

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 64.dp)
                        .semantics { contentDescription = contentDesc }
                        .clickable { onSelectTab(route) }
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        painter = painterResource(iconsAndCd.first),
                        contentDescription = null,
                        tint = tint,
                        modifier = Modifier.size(26.dp)
                    )
                    Text(
                        text = stringResource(labelRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = tint,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}