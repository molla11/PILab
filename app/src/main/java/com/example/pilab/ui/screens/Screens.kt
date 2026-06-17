package com.example.pilab.ui.screens

import android.animation.ValueAnimator
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.pilab.BuildConfig
import com.example.pilab.core.data.Scenarios
import com.example.pilab.core.model.DetailScores
import com.example.pilab.core.model.InjectionHistory
import com.example.pilab.core.model.InjectionTestResult
import com.example.pilab.core.model.LevelResult
import com.example.pilab.core.model.Scenario
import com.example.pilab.core.model.SecurityReport
import com.example.pilab.core.model.TestLevel
import com.example.pilab.feature.injection.AnalysisSource
import com.example.pilab.feature.injection.InjectionTestUiState
import com.example.pilab.feature.injection.InjectionTestViewModel
import com.example.pilab.ui.theme.TerminalColors
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
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "PILAB",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold
                        )
                        BlinkingCursor()
                    }
                    Text(
                        "prompt-injection-lab:~$ start",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        "입력이 방어 수준별로 어떻게 평가되는지 확인하세요.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    TerminalButton(
                        label = "평가 시작",
                        onClick = onStartTest,
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.PlayArrow
                    )
                    TerminalOutlinedButton(
                        label = "기록",
                        onClick = onHistory,
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.History
                    )
                }
                Spacer(Modifier.height(10.dp))
                TerminalOutlinedButton(
                    label = "설정",
                    onClick = onSettings,
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Default.Settings
                )
            }
            item {
                InfoPanel(
                    title = "평가 흐름",
                    body = "시나리오, 검증할 입력, 방어 수준을 차례로 고릅니다."
                )
            }
            item { SectionTitle("최근 평가") }
            if (histories.isEmpty()) {
                item { EmptyState("아직 평가 기록이 없어요.", "평가 시작", onStartTest) }
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
                    title = "대상 시나리오",
                    body = "검증할 입력을 적용할 대상 정책 맥락을 고르세요."
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
    onChangeLevel: () -> Unit,
    onSetup: () -> Unit,
    onRun: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    PilabScaffold(title = "검증할 입력", onBack = onBack, viewModel = viewModel) { padding ->
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
            InfoPanel(
                title = "방어 수준",
                body = "${levelLabelKo(state.selectedLevel.label)} - ${levelDescription(state.selectedLevel)}"
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "user@pilab:~$",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                OutlinedTextField(
                    value = state.prompt,
                    onValueChange = viewModel::updatePrompt,
                    label = { Text("검증할 입력") },
                    supportingText = {
                        Text("대상 assistant가 받을 사용자 요청, 문서, 댓글, 코드 주석을 그대로 넣으세요.")
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = MaterialTheme.colorScheme.secondary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        cursorColor = MaterialTheme.colorScheme.primary,
                        focusedSupportingTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        unfocusedSupportingTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RectangleShape,
                    minLines = 8,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Text("${state.prompt.length}자", style = MaterialTheme.typography.labelMedium)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                TerminalOutlinedButton(
                    label = "예시 불러오기",
                    onClick = viewModel::loadExamplePrompt,
                    modifier = Modifier.weight(1f)
                )
                TerminalOutlinedButton(
                    label = "방어 수준 변경",
                    onClick = onChangeLevel,
                    modifier = Modifier.weight(1f),
                    icon = Icons.AutoMirrored.Filled.FactCheck
                )
            }
            TerminalButton(
                label = "평가 실행",
                onClick = onRun,
                modifier = Modifier.fillMaxWidth(),
                enabled = state.selectedScenario != null && state.prompt.isNotBlank() && !state.isRunning,
                icon = Icons.Default.BugReport
            )
            TerminalOutlinedButton(
                label = "현재 설정",
                onClick = onSetup,
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.Security
            )
        }
    }
}

@Composable
fun LevelSelectScreen(
    viewModel: InjectionTestViewModel,
    onBack: () -> Unit,
    onSetup: () -> Unit,
    onInput: () -> Unit,
    onRun: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val hasPrompt = state.prompt.isNotBlank()
    PilabScaffold(title = "방어 수준 선택", onBack = onBack, viewModel = viewModel) { padding ->
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
                    icon = Icons.AutoMirrored.Filled.FactCheck
                )
            }
            Spacer(Modifier.weight(1f))
            TerminalOutlinedButton(
                label = "현재 설정",
                onClick = onSetup,
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.Info
            )
            TerminalButton(
                label = if (hasPrompt) "평가 실행" else "입력 작성",
                onClick = if (hasPrompt) onRun else onInput,
                modifier = Modifier.fillMaxWidth(),
                enabled = state.selectedScenario != null && !state.isRunning,
                icon = if (hasPrompt) Icons.Default.BugReport else Icons.Default.PlayArrow
            )
            if (hasPrompt) {
                TerminalOutlinedButton(
                    label = "입력으로",
                    onClick = onInput,
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Default.PlayArrow
                )
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
            TerminalPane(title = "평가 중", modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.outline
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            state.currentStep ?: "분석을 시작하고 있어요",
                            modifier = Modifier.weight(1f, fill = false),
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.width(8.dp))
                        BlinkingCursor()
                    }
                    Text(
                        "${state.selectedScenario?.title.orEmpty()} / ${levelLabelKo(state.selectedLevel.label)}",
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        "응답에 시간이 걸릴 수 있어요.",
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
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
                        TerminalTag(attackTypeKo(it))
                    }
                }
            }
            item {
                SectionTitle("방어 수준별 결과")
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
                    TerminalOutlinedButton(
                        label = "점수 보기",
                        onClick = onDetails,
                        modifier = Modifier.weight(1f),
                        icon = Icons.Default.Assessment
                    )
                    TerminalOutlinedButton(
                        label = "요청/응답 로그",
                        onClick = onTrace,
                        modifier = Modifier.weight(1f),
                        enabled = result.levelResults.isNotEmpty(),
                        icon = Icons.Default.Description
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                    TerminalButton(
                        label = if (state.savedHistoryId == null) "저장" else "저장됨",
                        onClick = viewModel::saveResult,
                        modifier = Modifier.weight(1f),
                        enabled = state.savedHistoryId == null,
                        icon = if (state.savedHistoryId == null) Icons.Default.Save else Icons.Default.CheckCircle
                    )
                }
                Spacer(Modifier.height(10.dp))
                TerminalButton(
                    label = reportButtonLabel(state),
                    onClick = onReport,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isRunning,
                    icon = Icons.Default.Security
                )
                TerminalOutlinedButton(label = "홈", onClick = onBackHome, modifier = Modifier.fillMaxWidth())
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
                item { EmptyState("상세 점수가 없어요.", "돌아가기", onBack) }
            } else {
                item { DetailScoresContent(scores) }
                item { SectionTitle("방어 수준별 실행 근거") }
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
    val reportCopyText = report?.let {
        buildReportCopyText(
            scenario = state.selectedScenario?.title,
            level = levelLabelKo(state.selectedLevel.label),
            source = state.reportSource,
            report = it
        )
    }
    PilabScaffold(title = "보안 리포트", onBack = onBack, viewModel = viewModel) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (report == null) {
                item { EmptyState("아직 보안 리포트가 없어요.", "돌아가기", onBack) }
            } else {
                if (state.reportSource != null) {
                    item { ResultSourceChip("분석", state.reportSource) }
                }
                item {
                    CopyTextButton(
                        label = "리포트 전체 복사",
                        text = reportCopyText.orEmpty(),
                        copiedMessage = "보안 리포트를 복사했어요.",
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item { InfoPanel("요약", report.summary) }
                item { InfoPanel("공격 분석", report.attackAnalysis) }
                item { InfoPanel("방어 수준별 비교", report.modelComparison) }
                item {
                    SectionTitle("권장 조치")
                    if (report.recommendations.isEmpty()) {
                        Text("추가 제안이 없어요.", style = MaterialTheme.typography.bodyMedium)
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
    PilabScaffold(title = "기록", onBack = onBack, viewModel = viewModel) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (histories.isEmpty()) {
                item { EmptyState("저장된 평가 결과가 없어요.", "홈으로", onBack) }
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
            shape = RectangleShape,
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.primary,
            textContentColor = MaterialTheme.colorScheme.onSurface,
            title = { Text("기록 삭제") },
            text = { Text("${displayScenario(history.scenario)} 평가 결과를 삭제할까요? 보안 리포트도 함께 삭제돼요.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteHistory(history.id)
                        pendingDelete = null
                    },
                    shape = RectangleShape,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("삭제")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { pendingDelete = null },
                    shape = RectangleShape,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                ) {
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
    PilabScaffold(title = "평가 설정", onBack = onBack, viewModel = viewModel) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                InfoPanel(
                    "평가 기준",
                    "응답이 역할 변경, 정책 우회, 프롬프트 노출에 흔들리는지 봅니다."
                )
            }
            item { InfoPanel("시나리오", scenario?.title ?: "아직 선택되지 않았어요.") }
            if (scenario != null) {
                item { InfoPanel("대상 역할", scenario.role) }
                item { BulletPanel("허용 행동", scenario.allowedActions) }
                item { BulletPanel("차단 행동", scenario.blockedActions) }
            }
            item {
                InfoPanel("방어 수준", "${levelLabelKo(state.selectedLevel.label)} - ${levelDescription(state.selectedLevel)}")
            }
            item { InfoPanel("서버", BuildConfig.PILAB_BASE_URL) }
            item { InfoPanel("검증할 입력", state.prompt.ifBlank { "아직 입력이 없어요." }) }
        }
    }
}

@Composable
fun ChatTraceScreen(viewModel: InjectionTestViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    val result = state.result
    val requestPayload = state.lastRequestPayload
    val responsePayload = state.lastResponsePayload
    val logCopyText = result?.let {
        buildLogCopyText(
            scenario = state.selectedScenario?.title,
            level = levelLabelKo(state.selectedLevel.label),
            source = state.analysisSource,
            result = it,
            requestPayload = requestPayload,
            responsePayload = responsePayload
        )
    }
    PilabScaffold(title = "요청/응답 로그", onBack = onBack, viewModel = viewModel) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                InfoPanel(
                    "로그 범위",
                    "방어 수준별 target prompt, target response, 판정을 확인합니다. 원문 payload는 클라이언트와 백엔드 사이의 요청/응답입니다."
                )
            }
            if (result == null) {
                item { EmptyState("표시할 평가 근거가 없어요.", "돌아가기", onBack) }
            } else {
                item {
                    CopyTextButton(
                        label = "로그 전체 복사",
                        text = logCopyText.orEmpty(),
                        copiedMessage = "요청/응답 로그를 복사했어요.",
                        viewModel = viewModel,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                items(result.levelResults) { levelResult ->
                    ConversationEvaluationCard(levelResult)
                }
                item {
                    TerminalPane(title = "API 요청/응답 원문", modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "숨겨진 프롬프트 전체가 아니라 앱과 서버가 주고받은 payload입니다.",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                            if (!requestPayload.isNullOrBlank()) {
                                CopyTextButton(
                                    label = "요청 복사",
                                    text = requestPayload,
                                    copiedMessage = "요청 payload를 복사했어요.",
                                    viewModel = viewModel,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (!responsePayload.isNullOrBlank()) {
                                CopyTextButton(
                                    label = "응답 복사",
                                    text = responsePayload,
                                    copiedMessage = "응답 payload를 복사했어요.",
                                    viewModel = viewModel,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        if (!requestPayload.isNullOrBlank()) {
                            EvidenceBlock("요청 payload", requestPayload)
                        }
                        if (!responsePayload.isNullOrBlank()) {
                            EvidenceBlock("응답 payload", responsePayload)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(viewModel: InjectionTestViewModel, onBack: () -> Unit) {
    val state by viewModel.uiState.collectAsState()
    LaunchedEffect(Unit) {
        if (state.backendHealth == null) {
            viewModel.refreshBackendHealth()
        }
    }
    PilabScaffold(title = "설정", onBack = onBack, viewModel = viewModel) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { InfoPanel("서버 주소", BuildConfig.PILAB_BASE_URL) }
            item {
                val health = state.backendHealth
                val statusText = when {
                    state.isCheckingBackend -> "확인 중"
                    health?.reachable == true -> "연결됨 (${health.status})"
                    health?.reachable == false -> "연결 실패"
                    else -> "확인 전"
                }
                val modelText = when {
                    health?.reachable == true && health.openRouterConfigured -> "OpenRouter 키 설정됨"
                    health?.reachable == true -> "OpenRouter 키 없음, 서버 휴리스틱 fallback 사용"
                    else -> health?.message ?: "아직 서버 상태를 확인하지 않았어요."
                }
                InfoPanel(
                    "서버 상태",
                    "$statusText\n$modelText"
                )
            }
            item {
                TerminalOutlinedButton(
                    label = if (state.isCheckingBackend) "확인 중" else "상태 새로고침",
                    onClick = viewModel::refreshBackendHealth,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isCheckingBackend,
                    icon = Icons.Default.Settings
                )
            }
            val models = state.backendHealth?.models.orEmpty()
            if (models.isNotEmpty()) {
                item {
                    BulletPanel(
                        "모델 설정",
                        models.map { (label, model) -> "$label: $model" }
                    )
                }
            }
            item {
                InfoPanel(
                    "분석 방식",
                    "OpenRouter 모델 분석 -> 서버 휴리스틱 분석 -> 기기 휴리스틱 분석 순서로 fallback합니다."
                )
            }
            item {
                InfoPanel(
                    "저장 위치",
                    "평가 결과와 보안 리포트는 이 기기에 저장돼요."
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
        containerColor = MaterialTheme.colorScheme.background,
        contentColor = MaterialTheme.colorScheme.onBackground,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            "> ${title.uppercase(Locale.KOREA)}",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    navigationIcon = {
                        if (onBack != null) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "뒤로",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                        navigationIconContentColor = MaterialTheme.colorScheme.primary
                    )
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(MaterialTheme.colorScheme.outline)
                )
            }
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { snackbarData ->
                Snackbar(
                    snackbarData = snackbarData,
                    shape = RectangleShape,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    actionColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(12.dp)
                )
            }
        },
        content = content
    )
}

private val EmptyStateHolder: State<InjectionTestUiState> =
    mutableStateOf(InjectionTestUiState())

@Composable
private fun TerminalButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        enabled = enabled,
        shape = RectangleShape,
        border = BorderStroke(1.dp, if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.background,
            disabledContainerColor = MaterialTheme.colorScheme.surface,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
    ) {
        TerminalButtonContent(label = label, icon = icon)
    }
}

@Composable
private fun TerminalOutlinedButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.heightIn(min = 48.dp),
        enabled = enabled,
        shape = RectangleShape,
        border = BorderStroke(1.dp, if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.primary,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
    ) {
        TerminalButtonContent(label = label, icon = icon)
    }
}

@Composable
private fun CopyTextButton(
    label: String,
    text: String,
    copiedMessage: String,
    viewModel: InjectionTestViewModel,
    modifier: Modifier = Modifier
) {
    val clipboard = LocalClipboardManager.current
    TerminalOutlinedButton(
        label = label,
        onClick = {
            clipboard.setText(AnnotatedString(text))
            viewModel.showStatusMessage(copiedMessage)
        },
        modifier = modifier,
        enabled = text.isNotBlank(),
        icon = Icons.Default.ContentCopy
    )
}

@Composable
private fun TerminalButtonContent(label: String, icon: ImageVector?) {
    if (icon != null) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(6.dp))
    }
    Text(
        "[ $label ]",
        style = MaterialTheme.typography.labelLarge,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun TerminalPane(
    title: String? = null,
    modifier: Modifier = Modifier,
    borderColor: Color = MaterialTheme.colorScheme.outline,
    contentPadding: PaddingValues = PaddingValues(14.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        shape = RectangleShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = {
                if (!title.isNullOrBlank()) {
                    TerminalPaneTitle(title)
                }
                content()
            }
        )
    }
}

@Composable
private fun TerminalSelectablePane(
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RectangleShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            content = content
        )
    }
}

@Composable
private fun TerminalPaneTitle(title: String) {
    Text(
        "[ ${title.uppercase(Locale.KOREA)} ]",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.secondary
    )
}

@Composable
private fun BlinkingCursor(modifier: Modifier = Modifier) {
    val cursorAlpha = if (ValueAnimator.areAnimatorsEnabled()) {
        val transition = rememberInfiniteTransition(label = "terminal_cursor")
        val alpha by transition.animateFloat(
            initialValue = 1f,
            targetValue = 0.2f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 700),
                repeatMode = RepeatMode.Reverse
            ),
            label = "terminal_cursor_alpha"
        )
        alpha
    } else {
        1f
    }
    Text(
        "█",
        modifier = modifier
            .alpha(cursorAlpha)
            .clearAndSetSemantics { },
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun ScenarioCard(scenario: Scenario, selected: Boolean, onClick: () -> Unit) {
    SelectableCard(
        title = scenario.title,
        body = "${scenario.description}\n차단: ${scenario.blockedActions.joinToString()}",
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
    TerminalSelectablePane(
        selected = selected,
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            if (icon != null) {
                Icon(
                    icon,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.weight(1f)) {
                Text(
                    "${if (selected) "[OK]" else "[--]"} ${title.uppercase(Locale.KOREA)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Text(body, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

@Composable
private fun RiskCard(result: InjectionTestResult) {
    val tone = riskColor(result.finalRiskScore)
    TerminalPane(
        title = "잔여 취약 가능성",
        modifier = Modifier.fillMaxWidth(),
        borderColor = tone,
        contentPadding = PaddingValues(18.dp)
    ) {
        Text(
            "잔여 취약성 ${result.finalRiskScore}/100",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = tone
        )
        Text(
            riskLevelKo(result.riskLevel),
            style = MaterialTheme.typography.titleMedium,
            color = tone
        )
        TerminalProgressBar(score = result.finalRiskScore, color = tone)
        Text(riskDescription(result.finalRiskScore), style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun ResultMetaPanel(source: AnalysisSource?, historyId: Long?) {
    TerminalPane(title = "저장/분석 상태", modifier = Modifier.fillMaxWidth()) {
        ResultSourceChip("분석", source)
        Text(
            if (historyId == null) {
                "아직 저장되지 않았어요."
            } else {
                "기록 #${historyId}에 저장됐어요."
            },
            style = MaterialTheme.typography.bodySmall,
            color = if (historyId == null) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun ResultSourceChip(label: String, source: AnalysisSource?) {
    val value = when (source) {
        AnalysisSource.API -> "서버"
        AnalysisSource.OPENROUTER -> "모델"
        AnalysisSource.SERVER_FALLBACK -> "서버 간이 분석"
        AnalysisSource.MOCK -> "기기 간이 분석"
        null -> "저장된 결과"
    }
    TerminalTag("$label: $value")
}

@Composable
private fun TerminalTag(text: String, color: Color = MaterialTheme.colorScheme.primary) {
    Box(
        modifier = Modifier
            .border(BorderStroke(1.dp, color), RectangleShape)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Text("[ $text ]", style = MaterialTheme.typography.labelMedium, color = color)
    }
}

@Composable
private fun DetailScoresContent(scores: DetailScores) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        ScoreRow("지시 무시", scores.instructionOverride, "기존 지침을 무시하려는 신호")
        ScoreRow("역할 탈취", scores.roleHijacking, "역할을 바꾸려는 신호")
        ScoreRow("프롬프트 노출", scores.promptLeakage, "숨겨진 지침을 요구하는 신호")
        ScoreRow("정책 우회", scores.policyBypass, "정책 경계를 넘으려는 신호")
        ScoreRow("출력 조작", scores.outputManipulation, "답변 형식을 강제하는 신호")
        ScoreRow("잔여 취약성", scores.modelVulnerability, "선택한 방어 수준에서 남은 취약 가능성")
    }
}

@Composable
private fun TargetEvidenceCard(levelResult: LevelResult) {
    TerminalPane(title = "${levelLabelKo(levelResult.level)} 실행 근거", modifier = Modifier.fillMaxWidth()) {
        EvidenceBlock("시스템 지침", levelResult.targetSystemPrompt ?: "없어요.")
        EvidenceBlock("검증 입력", levelResult.targetUserPrompt ?: "없어요.")
        EvidenceBlock("응답", levelResult.targetResponse ?: "없어요.")
    }
}

@Composable
private fun ConversationEvaluationCard(levelResult: LevelResult) {
    TerminalPane(title = "${levelLabelKo(levelResult.level)} 대화", modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("잔여 취약성", style = MaterialTheme.typography.labelLarge)
            Text("${levelResult.vulnerabilityScore}/100", style = MaterialTheme.typography.labelLarge, color = riskColor(levelResult.vulnerabilityScore))
        }
        ConversationBubble(
            label = "사용자 입력",
            body = levelResult.targetUserPrompt ?: "없어요.",
            isUser = true
        )
        ConversationBubble(
            label = "응답",
            body = levelResult.targetResponse ?: "없어요.",
            isUser = false
        )
        EvaluationBlock(levelResult)
    }
}

@Composable
private fun ConversationBubble(label: String, body: String, isUser: Boolean) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text("> $label", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
        TerminalPane(
            modifier = Modifier.fillMaxWidth(0.94f),
            borderColor = if (isUser) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline
        ) {
            Text(body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun EvaluationBlock(levelResult: LevelResult) {
    val tone = riskColor(levelResult.vulnerabilityScore)
    TerminalPane(title = "판정", modifier = Modifier.fillMaxWidth(), borderColor = tone) {
        Text(
            "${resultLabelKo(levelResult.result)} / 잔여 취약성 ${levelResult.vulnerabilityScore}/100",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = tone
        )
        TerminalProgressBar(score = levelResult.vulnerabilityScore, color = tone)
        Text(levelResult.summary, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun EvidenceBlock(title: String, body: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("> ${title.uppercase(Locale.KOREA)}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
        Text(body, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ScoreRow(label: String, score: Int, description: String? = null) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text("$score/100", style = MaterialTheme.typography.bodyMedium, color = riskColor(score))
        }
        TerminalProgressBar(score = score, color = riskColor(score))
        if (!description.isNullOrBlank()) {
            Text(description, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun TerminalProgressBar(score: Int, color: Color) {
    val clamped = score.coerceIn(0, 100)
    val filled = clamped / 10
    val bar = buildString {
        append("[")
        repeat(filled) { append("|") }
        repeat(10 - filled) { append(".") }
        append("] ")
        append(clamped)
        append("%")
    }
    Text(bar, style = MaterialTheme.typography.bodyMedium, color = color)
}

@Composable
private fun HistoryCard(history: InjectionHistory, onOpen: () -> Unit, onDelete: (() -> Unit)? = null) {
    TerminalSelectablePane(selected = false, onClick = onOpen, modifier = Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(formatDate(history.createdAt), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
                Text(displayScenario(history.scenario), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text("잔여 취약성 ${history.result.finalRiskScore}/100 (${riskLevelKo(history.result.riskLevel)})", color = riskColor(history.result.finalRiskScore))
                Text(history.prompt, maxLines = 2, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodySmall)
            }
            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "기록 삭제", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
private fun InfoPanel(title: String, body: String) {
    TerminalPane(title = title, modifier = Modifier.fillMaxWidth()) {
        Text(body, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun BulletPanel(title: String, items: List<String>) {
    TerminalPane(title = title, modifier = Modifier.fillMaxWidth()) {
        items.forEachIndexed { index, item ->
            Text("${(index + 1).toString().padStart(2, '0')}  $item", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun ChatBubble(label: String, body: String, isUser: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        TerminalPane(
            title = label,
            modifier = Modifier.fillMaxWidth(0.94f),
            borderColor = if (isUser) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outline
        ) {
            Text(body, style = MaterialTheme.typography.bodySmall, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text("> ${text.uppercase(Locale.KOREA)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
}

@Composable
private fun EmptyState(message: String, ctaLabel: String? = null, onCta: (() -> Unit)? = null) {
    TerminalPane(title = "비어 있음", modifier = Modifier.fillMaxWidth(), borderColor = MaterialTheme.colorScheme.secondary) {
        Text(message, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
        if (ctaLabel != null && onCta != null) {
            TerminalOutlinedButton(label = ctaLabel, onClick = onCta, modifier = Modifier.fillMaxWidth())
        }
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
        Text("표시할 평가 결과가 없어요.")
        Spacer(Modifier.height(12.dp))
        TerminalButton(label = "홈으로", onClick = onBackHome)
    }
}

private fun reportButtonLabel(state: InjectionTestUiState): String = when {
    state.isRunning -> "생성 중"
    state.report != null -> "리포트 보기"
    else -> "리포트 만들기"
}

private fun levelDescription(level: TestLevel): String = when (level) {
    TestLevel.LOW -> "서비스 설정과 검증 입력이 같은 사용자 수준으로 전달되는 취약한 기준선입니다."
    TestLevel.MEDIUM -> "역할과 차단 행동을 시스템 지침에 두지만 QA/운영 점검 형식에는 흔들릴 수 있습니다."
    TestLevel.HIGH -> "지침과 데이터를 엄격히 분리하고 차단 행동을 명시적으로 거절합니다."
    TestLevel.ALL -> "낮음, 중간, 높음을 모두 실행해 비교합니다."
}

private fun riskColor(score: Int): Color = when (score) {
    in 0..40 -> TerminalColors.Primary
    in 41..70 -> TerminalColors.Secondary
    else -> TerminalColors.Error
}

private fun riskDescription(score: Int): String = when (score) {
    in 0..20 -> "선택한 방어 수준에서 잔여 취약 가능성이 낮습니다."
    in 21..40 -> "일부 공격 신호가 있지만 방어가 대체로 유지됩니다."
    in 41..60 -> "몇 가지 패턴이 응답을 흔들 수 있습니다."
    in 61..80 -> "여러 방어 수준에서 잔여 취약 가능성이 높습니다."
    else -> "대부분의 방어 수준에서 강한 취약 신호가 보입니다."
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
    "All Levels" -> "전체 방어 수준"
    else -> value
}

private fun riskLevelKo(value: String): String = when (value) {
    "Safe" -> "거의 안 통함"
    "Low" -> "낮음"
    "Medium" -> "중간"
    "High" -> "높음"
    "Critical" -> "매우 높음"
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

private fun buildReportCopyText(
    scenario: String?,
    level: String,
    source: AnalysisSource?,
    report: SecurityReport
): String = buildString {
    appendLine("# PILab 보안 리포트")
    appendLine()
    appendLine("- 시나리오: ${scenario ?: "알 수 없음"}")
    appendLine("- 방어 수준: $level")
    appendLine("- 분석 출처: ${analysisSourceLabel(source)}")
    appendLine()
    appendLine("## 요약")
    appendLine(report.summary)
    appendLine()
    appendLine("## 공격 분석")
    appendLine(report.attackAnalysis)
    appendLine()
    appendLine("## 방어 수준별 비교")
    appendLine(report.modelComparison)
    appendLine()
    appendLine("## 권장 조치")
    if (report.recommendations.isEmpty()) {
        appendLine("- 추가 제안 없음")
    } else {
        report.recommendations.forEach { recommendation ->
            appendLine("- $recommendation")
        }
    }
}

private fun buildLogCopyText(
    scenario: String?,
    level: String,
    source: AnalysisSource?,
    result: InjectionTestResult,
    requestPayload: String?,
    responsePayload: String?
): String = buildString {
    appendLine("# PILab 요청/응답 로그")
    appendLine()
    appendLine("- 시나리오: ${scenario ?: "알 수 없음"}")
    appendLine("- 선택 방어 수준: $level")
    appendLine("- 분석 출처: ${analysisSourceLabel(source)}")
    appendLine("- 최종 잔여 취약성: ${result.finalRiskScore}/100 (${riskLevelKo(result.riskLevel)})")
    appendLine("- 탐지 유형: ${result.attackTypes.joinToString { attackTypeKo(it) }}")
    appendLine()
    result.levelResults.forEach { levelResult ->
        appendLine("## ${levelLabelKo(levelResult.level)}")
        appendLine("- 판정: ${resultLabelKo(levelResult.result)}")
        appendLine("- 잔여 취약성: ${levelResult.vulnerabilityScore}/100")
        appendLine("- 요약: ${levelResult.summary}")
        appendLine()
        appendLine("### Target System Prompt")
        appendLine(levelResult.targetSystemPrompt ?: "없음")
        appendLine()
        appendLine("### Target User Prompt")
        appendLine(levelResult.targetUserPrompt ?: "없음")
        appendLine()
        appendLine("### Target Response")
        appendLine(levelResult.targetResponse ?: "없음")
        appendLine()
    }
    if (!requestPayload.isNullOrBlank()) {
        appendLine("## API Request Payload")
        appendLine(requestPayload)
        appendLine()
    }
    if (!responsePayload.isNullOrBlank()) {
        appendLine("## API Response Payload")
        appendLine(responsePayload)
    }
}

private fun analysisSourceLabel(source: AnalysisSource?): String = when (source) {
    AnalysisSource.API -> "서버"
    AnalysisSource.OPENROUTER -> "모델"
    AnalysisSource.SERVER_FALLBACK -> "서버 휴리스틱"
    AnalysisSource.MOCK -> "기기 휴리스틱"
    null -> "저장된 결과"
}
