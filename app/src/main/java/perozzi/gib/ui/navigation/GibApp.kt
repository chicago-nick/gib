package perozzi.gib.ui.navigation

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.delay
import perozzi.gib.AppContainer
import perozzi.gib.ui.daily.DailyLogScreen
import perozzi.gib.ui.daily.DailyLogViewModel
import perozzi.gib.ui.history.HistoryScreen
import perozzi.gib.ui.history.HistoryViewModel
import perozzi.gib.ui.me.MeScreen
import perozzi.gib.ui.me.MeViewModel
import perozzi.gib.ui.trends.TrendsScreen
import perozzi.gib.ui.trends.TrendsViewModel

@Composable
fun GibApp(container: AppContainer) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route.orEmpty()
    val showBottomBar = currentRoute != GibDestination.Splash.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomDestinations.forEach { destination ->
                        NavigationBarItem(
                            selected = currentRoute.startsWith(destination.route),
                            onClick = {
                                navController.navigate(destination.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(destination.icon, contentDescription = destination.label) },
                            label = { Text(destination.label) },
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = GibDestination.Splash.route,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(GibDestination.Splash.route) {
                LaunchedEffect(Unit) {
                    delay(1_000)
                    navController.navigate(GibDestination.Daily.route) {
                        popUpTo(GibDestination.Splash.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
                SplashScreen()
            }
            composable(
                route = "daily?dateEpochDay={dateEpochDay}",
                arguments = listOf(navArgument("dateEpochDay") {
                    type = NavType.LongType
                    defaultValue = Long.MIN_VALUE
                }),
            ) { backStackEntry ->
                val dateEpochDay = backStackEntry.arguments?.getLong("dateEpochDay")?.takeIf { it != Long.MIN_VALUE }
                val viewModel: DailyLogViewModel = viewModel(
                    factory = DailyLogViewModel.factory(container, dateEpochDay)
                )
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                DailyLogScreen(
                    state = state,
                    onPreviousDay = viewModel::goToPreviousDay,
                    onNextDay = viewModel::goToNextDay,
                    onToday = viewModel::goToToday,
                    onAddMealPart = viewModel::addMealPart,
                    onQuickAdd = viewModel::addMealPart,
                    onRemoveMealPart = viewModel::removeMealPart,
                    onCopyYesterday = viewModel::copyYesterday,
                    onCopyBucketFromYesterday = viewModel::copyBucketFromYesterday,
                    onAlcoholChanged = viewModel::setAlcohol,
                    onExerciseChanged = viewModel::setExercise,
                    onWeightChanged = viewModel::setWeight,
                )
            }
            composable(GibDestination.History.route) {
                val viewModel: HistoryViewModel = viewModel(factory = HistoryViewModel.factory(container))
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                HistoryScreen(
                    state = state,
                    onToggleExpanded = viewModel::toggleExpanded,
                    onExpandAll = viewModel::expandAll,
                    onCollapseAll = viewModel::collapseAll,
                    onEditDay = { date ->
                        navController.navigate("daily?dateEpochDay=${date.toEpochDay()}")
                    },
                )
            }
            composable(GibDestination.Trends.route) {
                val viewModel: TrendsViewModel = viewModel(factory = TrendsViewModel.factory(container))
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                TrendsScreen(state = state)
            }
            composable(GibDestination.Me.route) {
                val viewModel: MeViewModel = viewModel(factory = MeViewModel.factory(container))
                val state by viewModel.uiState.collectAsStateWithLifecycle()
                MeScreen(
                    state = state,
                    onGoalSelected = viewModel::setGoal,
                    onBaseTargetChanged = viewModel::setBaseTarget,
                    onExerciseAdjustmentChanged = viewModel::setExerciseAdjustment,
                    onSave = viewModel::save,
                )
            }
        }
    }
}
