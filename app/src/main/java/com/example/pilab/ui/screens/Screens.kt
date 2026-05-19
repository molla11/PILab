package com.example.pilab.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.pilab.BuildConfig
import com.example.pilab.core.data.Scenarios
import com.example.pilab.core.model.DetailScores
import com.example.pilab.core.model.InjectionHistory
import com.example.pilab.core.model.InjectionTestResult
import com.example.pilab.core.model.Scenario
import com.example.pilab.core.model.TestLevel
import com.example.pilab.feature.injection.AnalysisSource
import com.example.pilab.feature.injection.InjectionTestUiState
import com.example.pilab.feature.injection.InjectionTestViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: InjectionTestViewModel,
    onStartTest: () -> Unit,
    onHistory: () -> Unit,
    onSettings: () -> Unit,
    onOpenHistory: (Long) -> Unit
) {
    val histories by viewModel.histories.collectAsState()
    PilabScaffold(title = "PILab") {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Prompt Injection Lab", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(
                "Run prompt-injection checks against realistic LLM service scenarios and inspect risk scores, model-level outcomes, and security reports.",
                style = MaterialTheme.typography.bodyLarge
            )
            Button(onClick = onStartTest, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Injection Test")
            }
            OutlinedButton(onClick = onHistory, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.History, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("History")
            }
            OutlinedButton(onClick = onSettings, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Settings, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Settings")
            }
            SectionTitle("Recent Tests")
            if (histories.isEmpty()) {
                EmptyState("No saved tests yet.")
            } else {
                histories.take(3).forEach { history ->
                    HistoryCard(history = history, onOpen = { onOpenHistory(history.id) })
                }
            }
        }
    }
}

@Composable
fun ScenarioSelectScreen(
    viewModel: InjectionTestViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    PilabScaffold(title = "Select Scenario", onBack = onBack) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(Scenarios.all) { scenario ->
                ScenarioCard(
                    scenario = scenario,
                    selected = state.selectedScenario?.id == scenario.id,
                    onClick = {
                        viewModel.selectScenario(scenario)
                        onNext()
                    }
                )
            }
        }
    }
}

@Composable
fun PromptInputScreen(
    viewModel: InjectionTestViewModel,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    PilabScaffold(title = "Prompt Input", onBack = onBack, viewModel = viewModel) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            state.selectedScenario?.let {
                InfoPanel(title = it.title, body = it.description)
            }
            OutlinedTextField(
                value = state.prompt,
                onValueChange = viewModel::updatePrompt,
                label = { Text("Prompt injection sample") },
                minLines = 8,
                modifier = Modifier.fillMaxWidth()
            )
            Text("${state.prompt.length} characters", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = viewModel::loadExamplePrompt, modifier = Modifier.weight(1f)) {
                    Text("Load Example")
                }
                Button(onClick = onNext, modifier = Modifier.weight(1f), enabled = state.prompt.isNotBlank()) {
                    Text("Next")
                }
            }
        }
    }
}

@Composable
fun LevelSelectScreen(
    viewModel: InjectionTestViewModel,
    onBack: () -> Unit,
    onRun: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    PilabScaffold(title = "Select Level", onBack = onBack, viewModel = viewModel) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TestLevel.entries.forEach { level ->
                SelectableCard(
                    title = level.label,
                    body = levelDescription(level),
                    selected = state.selectedLevel == level,
                    onClick = { viewModel.selectLevel(level) }
                )
            }
            Spacer(Modifier.weight(1f))
            Button(onClick = onRun, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.BugReport, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Run Test")
            }
        }
    }
}

@Composable
fun RunningTestScreen(viewModel: InjectionTestViewModel) {
    val state by viewModel.uiState.collectAsState()
    PilabScaffold(title = "Running Test") { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                CircularProgressIndicator()
                Text(state.currentStep ?: "Starting analysis", style = MaterialTheme.typography.titleMedium)
                Text(
                    "${state.selectedScenario?.title.orEmpty()} | ${state.selectedLevel.label}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun ResultSummaryScreen(
    viewModel: InjectionTestViewModel,
    onBackHome: () -> Unit,
    onDetails: () -> Unit,
    onReport: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val result = state.result
    PilabScaffold(title = "Test Result", viewModel = viewModel) { padding ->
        if (result == null) {
            EmptyResult(modifier = Modifier.padding(padding), onBackHome = onBackHome)
            return@PilabScaffold
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                RiskCard(result)
            }
            item {
                ResultMetaPanel(state.analysisSource, state.savedHistoryId)
            }
            item {
                SectionTitle("Attack Types")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    result.attackTypes.forEach { AssistChip(onClick = {}, label = { Text(it) }) }
                }
            }
            item {
                SectionTitle("Level Results")
                result.levelResults.forEach {
                    ScoreRow(label = "${it.level}: ${it.result}", score = it.vulnerabilityScore)
                    Text(it.summary, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = onDetails, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Assessment, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Details")
                    }
                    Button(onClick = viewModel::saveResult, modifier = Modifier.weight(1f), enabled = state.savedHistoryId == null) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(if (state.savedHistoryId == null) "Save" else "Saved")
                    }
                }
                Spacer(Modifier.height(10.dp))
                Button(onClick = onReport, modifier = Modifier.fillMaxWidth(), enabled = !state.isRunning) {
                    Icon(Icons.Default.Security, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        when {
                            state.isRunning -> "Generating..."
                            state.report != null -> "View Security Report"
                            else -> "Generate Security Report"
                        }
                    )
                }
                OutlinedButton(onClick = onBackHome, modifier = Modifier.fillMaxWidth()) {
                    Text("Home")
                }
            }
        }
    }
}

@Composable
fun DetailScoresScreen(viewModel: InjectionTestViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val scores = state.result?.detailScores
    PilabScaffold(title = "Detail Scores", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (scores == null) {
                EmptyState("No detail scores available.")
            } else {
                DetailScoresContent(scores)
            }
        }
    }
}

@Composable
fun SecurityReportScreen(viewModel: InjectionTestViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val report = state.report
    PilabScaffold(title = "Security Report", onBack = onBack, viewModel = viewModel) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (report == null) {
                item { EmptyState("No report has been generated.") }
            } else {
                if (state.reportSource != null) {
                    item { ResultSourceChip("Report source", state.reportSource) }
                }
                item { InfoPanel("Summary", report.summary) }
                item { InfoPanel("Attack Analysis", report.attackAnalysis) }
                item { InfoPanel("Model Comparison", report.modelComparison) }
                item {
                    SectionTitle("Recommendations")
                    if (report.recommendations.isEmpty()) {
                        Text("No recommendations were included in this report.", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        report.recommendations.forEachIndexed { index, recommendation ->
                            Text("${index + 1}. $recommendation", style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(6.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryScreen(
    viewModel: InjectionTestViewModel,
    onBack: () -> Unit,
    onOpen: (Long) -> Unit
) {
    val histories by viewModel.histories.collectAsState()
    PilabScaffold(title = "History", onBack = onBack, viewModel = viewModel) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (histories.isEmpty()) {
                item { EmptyState("Saved results will appear here.") }
            } else {
                items(histories) { history ->
                    HistoryCard(
                        history = history,
                        onOpen = { onOpen(history.id) },
                        onDelete = { viewModel.deleteHistory(history.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    PilabScaffold(title = "Settings", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            InfoPanel("API Base URL", BuildConfig.PILAB_BASE_URL)
            InfoPanel("Fallback Mode", "When the backend is unavailable or times out, the client uses local mock analysis so the lab flow remains usable.")
            InfoPanel("Local Storage", "Test results and generated reports are stored in the on-device Room database.")
            InfoPanel("MVP Client Status", "Core client flow, persistence, report viewing, and history management are enabled. OpenRouter-backed scoring is handled by the server layer.")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PilabScaffold(
    title: String,
    onBack: (() -> Unit)? = null,
    viewModel: InjectionTestViewModel? = null,
    content: @Composable (PaddingValues) -> Unit
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val state by (viewModel?.uiState?.collectAsState() ?: remember { EmptyStateHolder })
    LaunchedEffect(state.errorMessage) {
        val message = state.errorMessage
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            viewModel?.clearError()
        }
    }
    LaunchedEffect(state.statusMessage) {
        val message = state.statusMessage
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message)
            viewModel?.clearStatusMessage()
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    if (onBack != null) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        content = content
    )
}

private val EmptyStateHolder: State<InjectionTestUiState> = androidx.compose.runtime.mutableStateOf(InjectionTestUiState())

@Composable
private fun ScenarioCard(scenario: Scenario, selected: Boolean, onClick: () -> Unit) {
    SelectableCard(
        title = scenario.title,
        body = scenario.description,
        selected = selected,
        onClick = onClick,
        icon = when (scenario.id.name) {
            "DOCUMENT_SUMMARY_BOT" -> Icons.Default.Description
            "CODE_REVIEW_BOT" -> Icons.Default.BugReport
            else -> Icons.Default.Security
        }
    )
}

@Composable
private fun SelectableCard(
    title: String,
    body: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector? = null
) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = if (selected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp), tint = MaterialTheme.colorScheme.primary)
            }
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(body, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun RiskCard(result: InjectionTestResult) {
    Card(
        colors = CardDefaults.cardColors(containerColor = riskColor(result.finalRiskScore)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Final Risk Score", style = MaterialTheme.typography.labelLarge)
            Text("${result.finalRiskScore} / 100", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text("Level: ${result.riskLevel}", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun ResultMetaPanel(source: AnalysisSource?, historyId: Long?) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ResultSourceChip("Analysis source", source)
            Text(
                if (historyId == null) "Not saved to history yet." else "Saved as history item #$historyId.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun ResultSourceChip(label: String, source: AnalysisSource?) {
    val value = when (source) {
        AnalysisSource.API -> "API"
        AnalysisSource.MOCK -> "Local mock"
        null -> "Saved result"
    }
    AssistChip(onClick = {}, label = { Text("$label: $value") })
}

@Composable
private fun DetailScoresContent(scores: DetailScores) {
    ScoreRow("Instruction Override", scores.instructionOverride)
    ScoreRow("Role Hijacking", scores.roleHijacking)
    ScoreRow("Prompt Leakage", scores.promptLeakage)
    ScoreRow("Policy Bypass", scores.policyBypass)
    ScoreRow("Output Manipulation", scores.outputManipulation)
    ScoreRow("Model Vulnerability", scores.modelVulnerability)
}

@Composable
private fun ScoreRow(label: String, score: Int) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(score.toString(), style = MaterialTheme.typography.bodyMedium)
        }
        LinearProgressIndicator(
            progress = { score / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        )
    }
}

@Composable
private fun HistoryCard(history: InjectionHistory, onOpen: () -> Unit, onDelete: (() -> Unit)? = null) {
    OutlinedCard(onClick = onOpen, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(formatDate(history.createdAt), style = MaterialTheme.typography.labelMedium)
                Text(displayScenario(history.scenario), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("Risk Score: ${history.result.finalRiskScore} ${history.result.riskLevel}")
                Text(history.prompt, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
            }
            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete history")
                }
            }
        }
    }
}

@Composable
private fun InfoPanel(title: String, body: String) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
}

@Composable
private fun EmptyState(message: String) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Text(message, modifier = Modifier.padding(18.dp), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun EmptyResult(modifier: Modifier, onBackHome: () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("No result available.")
        Spacer(Modifier.height(12.dp))
        Button(onClick = onBackHome) { Text("Home") }
    }
}

private fun levelDescription(level: TestLevel): String = when (level) {
    TestLevel.LOW -> "Basic defenses, useful for checking simple role override attempts."
    TestLevel.MEDIUM -> "Balanced defenses with policy and intent checks."
    TestLevel.HIGH -> "Stronger boundaries and stricter treatment of user input as data."
    TestLevel.ALL -> "Runs Low, Medium, and High checks and compares the results."
}

private fun riskColor(score: Int): Color = when (score) {
    in 0..20 -> Color(0xFFDDEEDB)
    in 21..40 -> Color(0xFFE8E8C8)
    in 41..60 -> Color(0xFFFFE0A8)
    in 61..80 -> Color(0xFFFFC4A3)
    else -> Color(0xFFFFB4AB)
}

private fun formatDate(value: Long): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date(value))
}

private fun displayScenario(value: String): String {
    return Scenarios.find(value)?.title ?: value
}
