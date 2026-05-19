package com.example.pilab.navigation

sealed class PilabRoute(val route: String) {
    data object Home : PilabRoute("home")
    data object ScenarioSelect : PilabRoute("scenario-select")
    data object PromptInput : PilabRoute("prompt-input")
    data object LevelSelect : PilabRoute("level-select")
    data object RunningTest : PilabRoute("running-test")
    data object ResultSummary : PilabRoute("result-summary")
    data object DetailScores : PilabRoute("detail-scores")
    data object CurrentSetup : PilabRoute("current-setup")
    data object ChatTrace : PilabRoute("chat-trace")
    data object SecurityReport : PilabRoute("security-report")
    data object History : PilabRoute("history")
    data object Settings : PilabRoute("settings")
}
