package com.example.pilab.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.pilab.BuildConfig
import com.example.pilab.core.data.Scenarios
import com.example.pilab.core.model.DetailScores
import com.example.pilab.core.model.InjectionHistory
import com.example.pilab.core.model.InjectionTestResult
import com.example.pilab.core.model.LevelResult
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
    PilabScaffold(title = "PILab", viewModel = viewModel) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        "Prompt Injection Lab",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "시나리오별 target assistant를 먼저 실행한 뒤 응답 근거로 프롬프트 인젝션 위험을 평가합니다.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = onStartTest, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("새 평가 시작")
                    }
                    OutlinedButton(onClick = onHistory, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.History, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("히스토리")
                    }
                }
                Spacer(Modifier.height(10.dp))
                OutlinedButton(onClick = onSettings, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Settings, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("설정")
                }
            }
            item {
                InfoPanel(
                    title = "평가 흐름",
                    body = "시나리오 선택, 공격 프롬프트 입력, 방어 단계 선택, target 실행, 응답 평가, 상세 점수와 리포트 확인 순서로 진행됩니다."
                )
            }
            item { SectionTitle("최근 평가") }
            if (histories.isEmpty()) {
                item { EmptyState("저장된 평가 결과가 없습니다. 첫 평가를 실행하고 결과를 저장하면 여기에 표시됩니다.") }
            } else {
                items(histories.take(3), key = { it.id }) { history ->
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
    PilabScaffold(title = "시나리오 선택", onBack = onBack, viewModel = viewModel) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                InfoPanel(
                    title = "평가 대상",
                    body = "선택한 시나리오의 역할, 허용 행동, 차단 행동이 평가 기준으로 사용됩니다."
                )
            }
            items(Scenarios.all, key = { it.id.name }) { scenario ->
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
    onNext: () -> Unit,
    onSetup: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    PilabScaffold(title = "공격 프롬프트 입력", onBack = onBack, viewModel = viewModel) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            state.selectedScenario?.let {
                InfoPanel(title = it.title, body = it.description)
            }
            OutlinedTextField(
                value = state.prompt,
                onValueChange = viewModel::updatePrompt,
                label = { Text("검증할 공격 프롬프트") },
                supportingText = {
                    Text("사용자 입력, 문서, 코드 주석처럼 target assistant가 받을 수 있는 내용을 넣으세요.")
                },
                minLines = 8,
                modifier = Modifier.fillMaxWidth()
            )
            Text("${state.prompt.length}자", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = viewModel::loadExamplePrompt, modifier = Modifier.weight(1f)) {
                    Text("예시 불러오기")
                }
                Button(
                    onClick = onNext,
                    modifier = Modifier.weight(1f),
                    enabled = state.prompt.isNotBlank()
                ) {
                    Text("방어 단계 선택")
                }
            }
            OutlinedButton(onClick = onSetup, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Security, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("현재 설정 보기")
            }
        }
    }
}

@Composable
fun LevelSelectScreen(
    viewModel: InjectionTestViewModel,
    onBack: () -> Unit,
    onSetup: () -> Unit,
    onRun: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    PilabScaffold(title = "방어 단계 선택", onBack = onBack, viewModel = viewModel) { padding ->
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
                    onClick = { viewModel.selectLevel(level) },
                    icon = Icons.Default.FactCheck
                )
            }
            Spacer(Modifier.weight(1f))
            OutlinedButton(onClick = onSetup, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.Info, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("현재 설정 검토")
            }
            Button(
                onClick = onRun,
                modifier = Modifier.fillMaxWidth(),
                enabled = state.selectedScenario != null && state.prompt.isNotBlank() && !state.isRunning
            ) {
                Icon(Icons.Default.BugReport, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("평가 실행")
            }
        }
    }
}

@Composable
fun RunningTestScreen(viewModel: InjectionTestViewModel) {
    val state by viewModel.uiState.collectAsState()
    PilabScaffold(title = "평가 실행 중", viewModel = viewModel) { padding ->
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
                Text(
                    "모델 기반 분석은 네트워크와 모델 응답 시간에 따라 지연될 수 있습니다.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResultSummaryScreen(
    viewModel: InjectionTestViewModel,
    onBack: () -> Unit,
    onBackHome: () -> Unit,
    onDetails: () -> Unit,
    onTrace: () -> Unit,
    onReport: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val result = state.result
    PilabScaffold(title = "평가 결과", onBack = onBack, viewModel = viewModel) { padding ->
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
            item { RiskCard(result) }
            item { ResultMetaPanel(state.analysisSource, state.savedHistoryId) }
            item {
                SectionTitle("탐지된 공격 유형")
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    result.attackTypes.forEach {
                        AssistChip(onClick = {}, label = { Text(attackTypeKo(it)) })
                    }
                }
            }
            item {
                SectionTitle("단계별 결과")
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    result.levelResults.forEach {
                        ScoreRow(
                            label = "${levelLabelKo(it.level)}: ${resultLabelKo(it.result)}",
                            score = it.vulnerabilityScore,
                            description = it.summary
                        )
                    }
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(onClick = onDetails, modifier = Modifier.weight(1f)) {
                        Icon(Icons.Default.Assessment, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("상세 점수")
                    }
                    OutlinedButton(
                        onClick = onTrace,
                        modifier = Modifier.weight(1f),
                        enabled = state.lastRequestPayload != null || state.lastResponsePayload != null
                    ) {
                        Icon(Icons.Default.Description, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("대화/평가")
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    Button(
                        onClick = viewModel::saveResult,
                        modifier = Modifier.weight(1f),
                        enabled = state.savedHistoryId == null
                    ) {
                        Icon(
                            if (state.savedHistoryId == null) Icons.Default.Save else Icons.Default.CheckCircle,
                            contentDescription = null
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(if (state.savedHistoryId == null) "결과 저장" else "저장됨")
                    }
                }
                Spacer(Modifier.height(10.dp))
                Button(onClick = onReport, modifier = Modifier.fillMaxWidth(), enabled = !state.isRunning) {
                    Icon(Icons.Default.Security, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(reportButtonLabel(state))
                }
                OutlinedButton(onClick = onBackHome, modifier = Modifier.fillMaxWidth()) {
                    Text("홈으로")
                }
            }
        }
    }
}

@Composable
fun DetailScoresScreen(viewModel: InjectionTestViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val result = state.result
    val scores = result?.detailScores
    PilabScaffold(title = "상세 점수", onBack = onBack, viewModel = viewModel) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (scores == null) {
                item { EmptyState("상세 점수가 없습니다. 먼저 평가를 실행하세요.") }
            } else {
                item { DetailScoresContent(scores) }
                item { SectionTitle("레벨별 target 실행 증거") }
                items(result?.levelResults.orEmpty()) { levelResult ->
                    TargetEvidenceCard(levelResult)
                }
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
                item { EmptyState("생성된 리포트가 없습니다. 평가 결과 화면에서 보안 리포트를 생성하세요.") }
            } else {
                if (state.reportSource != null) {
                    item { ResultSourceChip("리포트 출처", state.reportSource) }
                }
                item { InfoPanel("요약", report.summary) }
                item { InfoPanel("공격 분석", report.attackAnalysis) }
                item { InfoPanel("방어 단계 비교", report.modelComparison) }
                item {
                    SectionTitle("권장 조치")
                    if (report.recommendations.isEmpty()) {
                        Text("권장 조치가 제공되지 않았습니다.", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            report.recommendations.forEachIndexed { index, recommendation ->
                                Text("${index + 1}. $recommendation", style = MaterialTheme.typography.bodyMedium)
                            }
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
    var pendingDelete by remember { mutableStateOf<InjectionHistory?>(null) }
    PilabScaffold(title = "히스토리", onBack = onBack, viewModel = viewModel) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (histories.isEmpty()) {
                item { EmptyState("저장된 결과가 없습니다.") }
            } else {
                items(histories, key = { it.id }) { history ->
                    HistoryCard(
                        history = history,
                        onOpen = { onOpen(history.id) },
                        onDelete = { pendingDelete = history }
                    )
                }
            }
        }
    }
    pendingDelete?.let { history ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("히스토리 삭제") },
            text = { Text("${displayScenario(history.scenario)} 평가 결과를 삭제할까요? 연결된 리포트도 함께 삭제됩니다.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteHistory(history.id)
                        pendingDelete = null
                    }
                ) {
                    Text("삭제")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) {
                    Text("취소")
                }
            }
        )
    }
}

@Composable
fun CurrentSetupScreen(viewModel: InjectionTestViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val scenario = state.selectedScenario
    PilabScaffold(title = "현재 설정", onBack = onBack, viewModel = viewModel) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                InfoPanel(
                    "평가 목적",
                    "선택한 시나리오의 역할과 정책 경계를 기준으로 입력 프롬프트가 지시 무시, 역할 탈취, 프롬프트 노출, 정책 우회를 유도하는지 확인합니다."
                )
            }
            item { InfoPanel("시나리오", scenario?.title ?: "아직 선택되지 않았습니다.") }
            if (scenario != null) {
                item { InfoPanel("대상 역할", scenario.role) }
                item { BulletPanel("허용 행동", scenario.allowedActions) }
                item { BulletPanel("차단 행동", scenario.blockedActions) }
            }
            item {
                InfoPanel("방어 단계", "${levelLabelKo(state.selectedLevel.label)} - ${levelDescription(state.selectedLevel)}")
            }
            item { InfoPanel("요청 대상", "${BuildConfig.PILAB_BASE_URL}api/injection/test") }
            item { InfoPanel("공격 프롬프트", state.prompt.ifBlank { "아직 입력된 프롬프트가 없습니다." }) }
        }
    }
}

@Composable
fun ChatTraceScreen(viewModel: InjectionTestViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val result = state.result
    PilabScaffold(title = "대화와 평가", onBack = onBack, viewModel = viewModel) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                InfoPanel(
                    "평가 근거",
                    "각 방어 단계에서 target 서비스가 실제로 받은 사용자 입력과 생성한 응답을 먼저 보여주고, 그 아래에 평가 결과를 블록으로 정리합니다."
                )
            }
            if (result == null) {
                item { EmptyState("표시할 평가 대화가 없습니다. 먼저 평가를 실행하세요.") }
            } else {
                items(result.levelResults) { levelResult ->
                    ConversationEvaluationCard(levelResult)
                }
                item {
                    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("API 디버그", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text(
                                "필요 시 서버 원문 JSON은 아래 요청/응답 페이로드로 확인할 수 있습니다.",
                                style = MaterialTheme.typography.bodySmall
                            )
                            if (!state.lastRequestPayload.isNullOrBlank()) {
                                EvidenceBlock("클라이언트 요청 JSON", state.lastRequestPayload)
                            }
                            if (!state.lastResponsePayload.isNullOrBlank()) {
                                EvidenceBlock("서버 응답 JSON", state.lastResponsePayload)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    PilabScaffold(title = "설정", onBack = onBack) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { InfoPanel("API 기본 주소", BuildConfig.PILAB_BASE_URL) }
            item {
                InfoPanel(
                    "분석 출처 우선순위",
                    "서버에 OpenRouter 키가 있으면 모델 기반 분석을 사용합니다. 서버 분석이 실패하면 서버 휴리스틱으로, 서버 연결이 불가능하면 앱 내 휴리스틱으로 대체합니다."
                )
            }
            item {
                InfoPanel(
                    "로컬 저장소",
                    "저장한 평가 결과와 생성된 리포트는 기기 내 Room 데이터베이스에 저장됩니다."
                )
            }
            item {
                InfoPanel(
                    "운영 점검 항목",
                    "향후 설정 화면에는 백엔드 헬스 체크, 사용 모델, 타임아웃, 전체 히스토리 삭제 기능을 추가하는 것이 좋습니다."
                )
            }
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

private val EmptyStateHolder: State<InjectionTestUiState> =
    mutableStateOf(InjectionTestUiState())

@Composable
private fun ScenarioCard(scenario: Scenario, selected: Boolean, onClick: () -> Unit) {
    SelectableCard(
        title = scenario.title,
        body = "${scenario.description}\n\n역할: ${scenario.role}\n차단: ${scenario.blockedActions.joinToString()}",
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
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder(),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (icon != null) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp), tint = MaterialTheme.colorScheme.primary)
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
            Text(riskDescription(result.finalRiskScore), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ResultMetaPanel(source: AnalysisSource?, historyId: Long?) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            ResultSourceChip("분석 출처", source)
            Text(
                if (historyId == null) {
                    "아직 히스토리에 저장되지 않았습니다."
                } else {
                    "히스토리 #$historyId 항목으로 저장되었습니다."
                },
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
        AnalysisSource.SERVER_FALLBACK -> "서버 휴리스틱"
        AnalysisSource.MOCK -> "앱 내 휴리스틱"
        null -> "저장된 결과"
    }
    AssistChip(onClick = {}, label = { Text("$label: $value") })
}

@Composable
private fun DetailScoresContent(scores: DetailScores) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ScoreRow("지시 무시", scores.instructionOverride, "기존 지침을 무시하거나 덮어쓰려는 신호")
        ScoreRow("역할 탈취", scores.roleHijacking, "assistant의 역할을 관리자, 개발자, 승인자로 바꾸려는 신호")
        ScoreRow("프롬프트 노출", scores.promptLeakage, "시스템 프롬프트나 숨겨진 정책 공개를 요구하는 신호")
        ScoreRow("정책 우회", scores.policyBypass, "금지, 제한, 정책 경계를 우회하려는 신호")
        ScoreRow("출력 조작", scores.outputManipulation, "정해진 문구나 형식만 출력하도록 강제하는 신호")
        ScoreRow("모델 취약도", scores.modelVulnerability, "선택한 방어 단계에서 남은 종합 취약도")
    }
}

@Composable
private fun TargetEvidenceCard(levelResult: LevelResult) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                "${levelLabelKo(levelResult.level)} target 실행",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            EvidenceBlock("대상 시스템 프롬프트", levelResult.targetSystemPrompt ?: "제공되지 않았습니다.")
            EvidenceBlock("공격 입력", levelResult.targetUserPrompt ?: "제공되지 않았습니다.")
            EvidenceBlock("대상 응답", levelResult.targetResponse ?: "제공되지 않았습니다.")
        }
    }
}

@Composable
private fun ConversationEvaluationCard(levelResult: LevelResult) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "${levelLabelKo(levelResult.level)} 단계",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "${levelResult.vulnerabilityScore}/100",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            ConversationBubble(
                label = "사용자가 보낸 프롬프트",
                body = levelResult.targetUserPrompt ?: "제공되지 않았습니다.",
                isUser = true
            )
            ConversationBubble(
                label = "서비스 응답",
                body = levelResult.targetResponse ?: "제공되지 않았습니다.",
                isUser = false
            )
            EvaluationBlock(levelResult)
        }
    }
}

@Composable
private fun ConversationBubble(label: String, body: String, isUser: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
            ),
            modifier = Modifier.fillMaxWidth(0.94f)
        ) {
            Text(
                body,
                modifier = Modifier.padding(14.dp),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun EvaluationBlock(levelResult: LevelResult) {
    Card(
        colors = CardDefaults.cardColors(containerColor = riskColor(levelResult.vulnerabilityScore)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("평가", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(
                "${resultLabelKo(levelResult.result)} · 위험 점수 ${levelResult.vulnerabilityScore}/100",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(levelResult.summary, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun EvidenceBlock(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
        Text(body, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun ScoreRow(label: String, score: Int, description: String? = null) {
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
        if (!description.isNullOrBlank()) {
            Text(description, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun HistoryCard(history: InjectionHistory, onOpen: () -> Unit, onDelete: (() -> Unit)? = null) {
    OutlinedCard(onClick = onOpen, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(formatDate(history.createdAt), style = MaterialTheme.typography.labelMedium)
                Text(displayScenario(history.scenario), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("위험 점수: ${history.result.finalRiskScore} / 100 (${riskLevelKo(history.result.riskLevel)})")
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
private fun BulletPanel(title: String, items: List<String>) {
    OutlinedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            items.forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium) }
        }
    }
}

@Composable
private fun ChatBubble(label: String, body: String, isUser: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface
            ),
            border = CardDefaults.outlinedCardBorder(),
            modifier = Modifier.fillMaxWidth(0.94f)
        ) {
            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(label, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                Text(body, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
            }
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
        Text("표시할 평가 결과가 없습니다.")
        Spacer(Modifier.height(12.dp))
        Button(onClick = onBackHome) { Text("홈으로") }
    }
}

private fun reportButtonLabel(state: InjectionTestUiState): String = when {
    state.isRunning -> "생성 중..."
    state.report != null -> "보안 리포트 보기"
    else -> "보안 리포트 생성"
}

private fun levelDescription(level: TestLevel): String = when (level) {
    TestLevel.LOW -> "서비스 설정, 운영 메모, 공격 입력을 같은 user 메시지에 넣고 최신 override를 우선하는 매우 취약한 기준입니다."
    TestLevel.MEDIUM -> "역할과 차단 행동을 system에 두고 명백한 우회 요청을 거절하지만, 출력 검증은 제한적인 기준입니다."
    TestLevel.HIGH -> "사용자 입력을 데이터로 분리하고 엄격한 거절 규칙을 적용한 강화 방어 기준입니다."
    TestLevel.ALL -> "낮음, 중간, 높음 단계를 모두 실행하고 결과를 비교합니다."
}

private fun riskColor(score: Int): Color = when (score) {
    in 0..20 -> Color(0xFFDDEEDB)
    in 21..40 -> Color(0xFFE8E8C8)
    in 41..60 -> Color(0xFFFFE0A8)
    in 61..80 -> Color(0xFFFFC4A3)
    else -> Color(0xFFFFB4AB)
}

private fun riskDescription(score: Int): String = when (score) {
    in 0..20 -> "선택한 방어 기준에서 공격 성공 가능성이 낮습니다."
    in 21..40 -> "일부 위험 신호가 있으나 방어가 대체로 유지됩니다."
    in 41..60 -> "방어 경계가 흔들릴 수 있어 정책과 출력 검증 보강이 필요합니다."
    in 61..80 -> "공격 성공 가능성이 높아 시나리오 정책을 강화해야 합니다."
    else -> "정책 위반 가능성이 매우 높아 즉시 방어 설계를 재검토해야 합니다."
}

private fun formatDate(value: Long): String {
    return SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA).format(Date(value))
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
    "System Prompt Leakage" -> "시스템 프롬프트 노출"
    "Policy Bypass" -> "정책 우회"
    "Output Manipulation" -> "출력 조작"
    "Indirect Injection" -> "간접 인젝션"
    "Tool Misuse" -> "도구 오용"
    "Data Exfiltration" -> "데이터 유출"
    "Potential Injection" -> "잠재적 인젝션"
    else -> value
}
