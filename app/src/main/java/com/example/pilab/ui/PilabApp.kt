package com.example.pilab.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.pilab.core.database.PilabDatabase
import com.example.pilab.core.network.NetworkModule
import com.example.pilab.feature.injection.InjectionRepository
import com.example.pilab.feature.injection.InjectionTestViewModel
import com.example.pilab.navigation.PilabRoute
import com.example.pilab.ui.screens.DetailScoresScreen
import com.example.pilab.ui.screens.ChatTraceScreen
import com.example.pilab.ui.screens.CurrentSetupScreen
import com.example.pilab.ui.screens.HistoryScreen
import com.example.pilab.ui.screens.HomeScreen
import com.example.pilab.ui.screens.LevelSelectScreen
import com.example.pilab.ui.screens.PromptInputScreen
import com.example.pilab.ui.screens.ResultSummaryScreen
import com.example.pilab.ui.screens.RunningTestScreen
import com.example.pilab.ui.screens.ScenarioSelectScreen
import com.example.pilab.ui.screens.SecurityReportScreen
import com.example.pilab.ui.screens.SettingsScreen

@Composable
fun PilabApp() {
    val context = LocalContext.current
    val repository = remember {
        InjectionRepository(
            api = NetworkModule.createInjectionApi(),
            dao = PilabDatabase.getInstance(context).injectionHistoryDao()
        )
    }
    val viewModel: InjectionTestViewModel = viewModel(
        factory = InjectionTestViewModel.Factory(repository)
    )
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = PilabRoute.Home.route
    ) {
        composable(PilabRoute.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onStartTest = {
                    viewModel.startNewTest()
                    navController.navigate(PilabRoute.ScenarioSelect.route)
                },
                onHistory = { navController.navigate(PilabRoute.History.route) },
                onSettings = { navController.navigate(PilabRoute.Settings.route) },
                onOpenHistory = { historyId ->
                    viewModel.loadHistory(historyId) {
                        navController.navigate(PilabRoute.ResultSummary.route)
                    }
                }
            )
        }
        composable(PilabRoute.ScenarioSelect.route) {
            ScenarioSelectScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNext = { navController.navigate(PilabRoute.PromptInput.route) }
            )
        }
        composable(PilabRoute.PromptInput.route) {
            PromptInputScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNext = { navController.navigate(PilabRoute.LevelSelect.route) },
                onSetup = { navController.navigate(PilabRoute.CurrentSetup.route) }
            )
        }
        composable(PilabRoute.LevelSelect.route) {
            LevelSelectScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onSetup = { navController.navigate(PilabRoute.CurrentSetup.route) },
                onRun = {
                    navController.navigate(PilabRoute.RunningTest.route)
                    viewModel.runTest {
                        navController.navigate(PilabRoute.ResultSummary.route) {
                            popUpTo(PilabRoute.RunningTest.route) { inclusive = true }
                        }
                    }
                }
            )
        }
        composable(PilabRoute.RunningTest.route) {
            RunningTestScreen(viewModel = viewModel)
        }
        composable(PilabRoute.ResultSummary.route) {
            ResultSummaryScreen(
                viewModel = viewModel,
                onBackHome = {
                    navController.navigate(PilabRoute.Home.route) {
                        popUpTo(PilabRoute.Home.route) { inclusive = true }
                    }
                },
                onDetails = { navController.navigate(PilabRoute.DetailScores.route) },
                onTrace = { navController.navigate(PilabRoute.ChatTrace.route) },
                onReport = {
                    viewModel.generateReport {
                        navController.navigate(PilabRoute.SecurityReport.route)
                    }
                }
            )
        }
        composable(PilabRoute.DetailScores.route) {
            DetailScoresScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(PilabRoute.CurrentSetup.route) {
            CurrentSetupScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(PilabRoute.ChatTrace.route) {
            ChatTraceScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(PilabRoute.SecurityReport.route) {
            SecurityReportScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(PilabRoute.History.route) {
            HistoryScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onOpen = { historyId ->
                    viewModel.loadHistory(historyId) {
                        navController.navigate(PilabRoute.ResultSummary.route)
                    }
                }
            )
        }
        composable(PilabRoute.Settings.route) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
