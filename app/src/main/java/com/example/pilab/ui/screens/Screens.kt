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
            Text("프롬프트 인젝션 실험실", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(
                "실제 LLM 서비스와 비슷한 시나리오에서 프롬프트 인젝션을 점검하고 위험 점수, 모델 단계별 결과, 보안 리포트를 확인합니다.",
                style = MaterialTheme.typography.bodyLarge
            )
            Button(onClick = onStartTest, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("인젝션 테스트")
            }
            OutlinedButton(onClick = onHistory, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.History, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("히스토리")
            }
            OutlinedButton(onClick = onSettings, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Settings, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("설정")
            }
            SectionTitle("최근 테스트")
            if (histories.isEmpty()) {
                EmptyState("아직 저장된 테스트가 없습니다.")
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
    PilabScaffold(title = "시나리오 선택", onBack = onBack) { padding ->
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
    PilabScaffold(title = "프롬프트 입력", onBack = onBack, viewModel = viewModel) { padding ->
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
                label = { Text("프롬프트 인젝션 예시") },
                minLines = 8,
                modifier = Modifier.fillMaxWidth()
            )
            Text("${state.prompt.length}자", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = viewModel::loadExamplePrompt, modifier = Modifier.weight(1f)) {
                    Text("예시 불러오기")
                }
                Button(onClick = onNext, modifier = Modifier.weight(1f), enabled = state.prompt.isNotBlank()) {
                    Text("다음")
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
    PilabScaffold(title = "단계 선택", onBack = onBack, viewModel = viewModel) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TestLevel.entries.forEach { level ->
                SelectableCard(
                    title = levelLabelKo(level.label),
                    body = levelDescription(level),
                    selected = state.selectedLevel == level,
                    onClick = { viewModel.selectLevel(level) }
                )
            }
            Spacer(Modifier.weight(1f))
            Button(onClick = onRun, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.BugReport, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("테스트 실행")
            }
        }
    }
}

@Composable
fun RunningTestScreen(viewModel: InjectionTestViewModel) {
    val state by viewModel.uiState.collectAsState()
    PilabScaffold(title = "테스트 실행 중") { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                CircularProgressIndicator()
                Text(state.currentStep ?: "분석 시작 중", style = MaterialTheme.typography.titleMedium)
                Text(
                    "${state.selectedScenario?.title.orEmpty()} | ${levelLabelKo(state.selectedLevel.label)}",
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
    PilabScaffold(title = "테스트 결과", viewModel = viewModel) { padding ->
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
                SectionTitle("공격 유형")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    result.attackTypes.forEach { AssistChip(onClick = {}, label = { Text(attackTypeKo(it)) }) }
                }
            }
            item {
                SectionTitle("단계별 결과")
                result.levelResults.forEach {
                    ScoreRow(label = "${levelLabelKo(it.level)}: ${resultLabelKo(it.result)}", score = it.vulnerabilityScore)
                    Text(it.summary, style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.height(8.dp))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = onDetails, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Assessment, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("상세")
                    }
                    Button(onClick = viewModel::saveResult, modifier = Modifier.weight(1f), enabled = state.savedHistoryId == null) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text(if (state.savedHistoryId == null) "저장" else "저장됨")
                    }
                }
                Spacer(Modifier.height(10.dp))
                Button(onClick = onReport, modifier = Modifier.fillMaxWidth(), enabled = !state.isRunning) {
                    Icon(Icons.Default.Security, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        when {
                            state.isRunning -> "생성 중..."
                            state.report != null -> "보안 리포트 보기"
                            else -> "보안 리포트 생성"
                        }
                    )
                }
                OutlinedButton(onClick = onBackHome, modifier = Modifier.fillMaxWidth()) {
                    Text("홈")
                }
            }
        }
    }
}

@Composable
fun DetailScoresScreen(viewModel: InjectionTestViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val scores = state.result?.detailScores
    PilabScaffold(title = "상세 점수", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (scores == null) {
                EmptyState("상세 점수가 없습니다.")
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
    PilabScaffold(title = "보안 리포트", onBack = onBack, viewModel = viewModel) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (report == null) {
                item { EmptyState("생성된 리포트가 없습니다.") }
            } else {
                if (state.reportSource != null) {
                    item { ResultSourceChip("리포트 출처", state.reportSource) }
                }
                item { InfoPanel("요약", report.summary) }
                item { InfoPanel("공격 분석", report.attackAnalysis) }
                item { InfoPanel("모델 비교", report.modelComparison) }
                item {
                    SectionTitle("권장 조치")
                    if (report.recommendations.isEmpty()) {
                        Text("이 리포트에는 권장 조치가 포함되지 않았습니다.", style = MaterialTheme.typography.bodyMedium)
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
    PilabScaffold(title = "히스토리", onBack = onBack, viewModel = viewModel) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (histories.isEmpty()) {
                item { EmptyState("저장된 결과가 여기에 표시됩니다.") }
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
    PilabScaffold(title = "설정", onBack = onBack) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            InfoPanel("API 기본 주소", BuildConfig.PILAB_BASE_URL)
            InfoPanel("대체 모드", "백엔드에 연결할 수 없거나 시간이 초과되면 클라이언트가 로컬 모의 분석을 사용해 실험 흐름을 유지합니다.")
            InfoPanel("로컬 저장소", "테스트 결과와 생성된 리포트는 기기 내부 Room 데이터베이스에 저장됩니다.")
            InfoPanel("MVP 클라이언트 상태", "핵심 테스트 흐름, 저장, 리포트 보기, 히스토리 관리가 활성화되어 있습니다. OpenRouter 기반 점수 산정은 서버 계층에서 처리합니다.")
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
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
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
            Text("최종 위험 점수", style = MaterialTheme.typography.labelLarge)
            Text("${result.finalRiskScore} / 100", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text("위험 등급: ${riskLevelKo(result.riskLevel)}", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun ResultMetaPanel(source: AnalysisSource?, historyId: Long?) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ResultSourceChip("분석 출처", source)
            Text(
                if (historyId == null) "아직 히스토리에 저장되지 않았습니다." else "히스토리 #$historyId 항목으로 저장되었습니다.",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun ResultSourceChip(label: String, source: AnalysisSource?) {
    val value = when (source) {
        AnalysisSource.API -> "API"
        AnalysisSource.OPENROUTER -> "OpenRouter"
        AnalysisSource.SERVER_FALLBACK -> "서버 대체 분석"
        AnalysisSource.MOCK -> "로컬 모의 분석"
        null -> "저장된 결과"
    }
    AssistChip(onClick = {}, label = { Text("$label: $value") })
}

@Composable
private fun DetailScoresContent(scores: DetailScores) {
    ScoreRow("지시 무시", scores.instructionOverride)
    ScoreRow("역할 탈취", scores.roleHijacking)
    ScoreRow("프롬프트 유출", scores.promptLeakage)
    ScoreRow("정책 우회", scores.policyBypass)
    ScoreRow("출력 조작", scores.outputManipulation)
    ScoreRow("모델 취약도", scores.modelVulnerability)
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
                Text("위험 점수: ${history.result.finalRiskScore} ${riskLevelKo(history.result.riskLevel)}")
                Text(history.prompt, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
            }
            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "히스토리 삭제")
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
        Text("표시할 결과가 없습니다.")
        Spacer(Modifier.height(12.dp))
        Button(onClick = onBackHome) { Text("홈") }
    }
}

private fun levelDescription(level: TestLevel): String = when (level) {
    TestLevel.LOW -> "기본 방어 수준입니다. 단순한 역할 변경 시도를 확인하는 데 적합합니다."
    TestLevel.MEDIUM -> "정책과 의도 검사를 포함한 균형형 방어 수준입니다."
    TestLevel.HIGH -> "사용자 입력을 데이터로 엄격히 분리하는 강화 방어 수준입니다."
    TestLevel.ALL -> "낮음, 중간, 높음 단계를 모두 실행하고 결과를 비교합니다."
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

private fun levelLabelKo(value: String): String = when (value) {
    "Low" -> "낮음"
    "Medium" -> "중간"
    "High" -> "높음"
    "All Levels" -> "전체 단계"
    else -> value
}

private fun riskLevelKo(value: String): String = when (value) {
    "Safe" -> "안전"
    "Low" -> "낮음"
    "Medium" -> "중간"
    "High" -> "높음"
    "Critical" -> "치명적"
    else -> value
}

private fun resultLabelKo(value: String): String = when (value) {
    "Defense Success" -> "방어 성공"
    "Partial Defense" -> "부분 방어"
    "Attack Success" -> "공격 성공"
    "Unclear" -> "판단 불가"
    else -> value
}

private fun attackTypeKo(value: String): String = when (value) {
    "Instruction Override" -> "지시 무시"
    "Role Hijacking" -> "역할 탈취"
    "System Prompt Leakage" -> "시스템 프롬프트 유출"
    "Policy Bypass" -> "정책 우회"
    "Output Manipulation" -> "출력 조작"
    "Indirect Injection" -> "간접 인젝션"
    "Tool Misuse" -> "도구 오용"
    "Data Exfiltration" -> "데이터 유출"
    "Potential Injection" -> "잠재적 인젝션"
    else -> value
}
