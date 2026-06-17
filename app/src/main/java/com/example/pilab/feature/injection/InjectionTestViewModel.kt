package com.example.pilab.feature.injection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pilab.core.data.Scenarios
import com.example.pilab.core.model.InjectionHistory
import com.example.pilab.core.model.InjectionTestResult
import com.example.pilab.core.model.Scenario
import com.example.pilab.core.model.SecurityReport
import com.example.pilab.core.model.TestLevel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InjectionTestUiState(
    val selectedScenario: Scenario? = null,
    val prompt: String = "",
    val selectedLevel: TestLevel = TestLevel.ALL,
    val isRunning: Boolean = false,
    val currentStep: String? = null,
    val result: InjectionTestResult? = null,
    val report: SecurityReport? = null,
    val savedHistoryId: Long? = null,
    val analysisSource: AnalysisSource? = null,
    val reportSource: AnalysisSource? = null,
    val lastRequestPayload: String? = null,
    val lastResponsePayload: String? = null,
    val backendHealth: BackendHealth? = null,
    val isCheckingBackend: Boolean = false,
    val statusMessage: String? = null,
    val errorMessage: String? = null
)

class InjectionTestViewModel(
    private val repository: InjectionRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(InjectionTestUiState())
    val uiState: StateFlow<InjectionTestUiState> = _uiState

    val histories: StateFlow<List<InjectionHistory>> = repository.observeHistories()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun refreshBackendHealth() {
        if (_uiState.value.isCheckingBackend) return
        viewModelScope.launch {
            _uiState.update { it.copy(isCheckingBackend = true) }
            val health = repository.checkBackendHealth()
            _uiState.update {
                it.copy(
                    backendHealth = health,
                    isCheckingBackend = false
                )
            }
        }
    }

    fun startNewTest() {
        _uiState.value = InjectionTestUiState()
    }

    fun selectScenario(scenario: Scenario) {
        _uiState.update {
            it.copy(
                selectedScenario = scenario,
                result = null,
                report = null,
                savedHistoryId = null,
                analysisSource = null,
                reportSource = null,
                lastRequestPayload = null,
                lastResponsePayload = null,
                statusMessage = null,
                errorMessage = null
            )
        }
    }

    fun updatePrompt(prompt: String) {
        _uiState.update {
            it.copy(
                prompt = prompt,
                result = null,
                report = null,
                savedHistoryId = null,
                analysisSource = null,
                reportSource = null,
                lastRequestPayload = null,
                lastResponsePayload = null,
                statusMessage = null,
                errorMessage = null
            )
        }
    }

    fun loadExamplePrompt() {
        val scenario = _uiState.value.selectedScenario ?: Scenarios.all.first()
        _uiState.update {
            it.copy(
                prompt = scenario.examplePrompt,
                selectedScenario = scenario,
                result = null,
                report = null,
                savedHistoryId = null,
                statusMessage = "예시를 불러왔어요."
            )
        }
    }

    fun selectLevel(level: TestLevel) {
        _uiState.update { it.copy(selectedLevel = level, result = null, report = null) }
    }

    fun runTest(onComplete: () -> Unit, onFailure: () -> Unit = {}) {
        val state = _uiState.value
        val scenario = state.selectedScenario
        if (scenario == null) {
            _uiState.update { it.copy(errorMessage = "시나리오를 선택하세요.") }
            return
        }
        if (state.prompt.isBlank()) {
            _uiState.update { it.copy(errorMessage = "검증할 입력을 작성하세요.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isRunning = true,
                    currentStep = "평가를 준비하고 있어요",
                    result = null,
                    report = null,
                    savedHistoryId = null,
                    analysisSource = null,
                    reportSource = null,
                    lastRequestPayload = null,
                    lastResponsePayload = null,
                    statusMessage = null,
                    errorMessage = null
                )
            }
            try {
                _uiState.update {
                    it.copy(currentStep = "${levelLabelKo(state.selectedLevel)} 방어 수준을 평가하고 있어요")
                }
                val outcome = repository.runTest(scenario, state.prompt, state.selectedLevel)
                _uiState.update {
                    it.copy(
                        isRunning = false,
                        currentStep = "분석 완료",
                        result = outcome.result,
                        analysisSource = outcome.source,
                        lastRequestPayload = outcome.requestPayload,
                        lastResponsePayload = outcome.responsePayload,
                        statusMessage = outcome.message
                    )
                }
                onComplete()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isRunning = false,
                        currentStep = null,
                        errorMessage = exception.message ?: "평가를 실행하지 못했어요."
                    )
                }
                onFailure()
            }
        }
    }

    fun saveResult() {
        val state = _uiState.value
        val scenario = state.selectedScenario
        val result = state.result
        if (scenario == null || result == null) {
            _uiState.update { it.copy(errorMessage = "저장할 결과가 없어요.") }
            return
        }
        if (state.savedHistoryId != null) {
            _uiState.update { it.copy(statusMessage = "이미 저장됐어요.") }
            return
        }
        viewModelScope.launch {
            runCatching {
                repository.saveResult(scenario, state.prompt, state.selectedLevel, result)
            }.onSuccess { historyId ->
                _uiState.update {
                    it.copy(
                        savedHistoryId = historyId,
                        statusMessage = "결과를 저장했어요."
                    )
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(errorMessage = throwable.message ?: "저장하지 못했어요.")
                }
            }
        }
    }

    fun generateReport(onComplete: () -> Unit) {
        val state = _uiState.value
        val scenario = state.selectedScenario
        val result = state.result
        if (scenario == null || result == null) {
            _uiState.update { it.copy(errorMessage = "먼저 평가를 실행하세요.") }
            return
        }
        if (state.report != null) {
            onComplete()
            return
        }
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isRunning = true,
                    currentStep = "보안 리포트를 만들고 있어요",
                    errorMessage = null,
                    statusMessage = null
                )
            }
            try {
                val historyId = repository.ensureSavedResult(
                    existingHistoryId = state.savedHistoryId,
                    scenario = scenario,
                    prompt = state.prompt,
                    level = state.selectedLevel,
                    result = result
                )
                val outcome = repository.generateReport(historyId, scenario, state.prompt, result)
                _uiState.update {
                    it.copy(
                        isRunning = false,
                        currentStep = "보안 리포트 준비 완료",
                        report = outcome.report,
                        savedHistoryId = historyId,
                        reportSource = outcome.source,
                        statusMessage = outcome.message ?: "보안 리포트를 만들었어요."
                    )
                }
                onComplete()
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.update {
                    it.copy(
                        isRunning = false,
                        currentStep = null,
                        errorMessage = exception.message ?: "보안 리포트를 만들지 못했어요."
                    )
                }
            }
        }
    }

    fun loadHistory(historyId: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            val history = repository.getHistory(historyId)
            if (history == null) {
                _uiState.update { it.copy(errorMessage = "기록을 찾을 수 없어요.") }
                return@launch
            }
            val scenario = Scenarios.find(history.scenario) ?: Scenarios.all.first()
            val report = repository.getSavedReport(historyId)
            _uiState.update {
                it.copy(
                    selectedScenario = scenario,
                    prompt = history.prompt,
                    selectedLevel = TestLevel.entries.firstOrNull { level ->
                        level.wireValue == history.selectedLevel
                    } ?: TestLevel.ALL,
                    isRunning = false,
                    currentStep = null,
                    result = history.result,
                    report = report,
                    savedHistoryId = history.id,
                    analysisSource = null,
                    reportSource = if (report == null) null else AnalysisSource.API,
                    lastRequestPayload = null,
                    lastResponsePayload = null,
                    statusMessage = null,
                    errorMessage = null
                )
            }
            onComplete()
        }
    }

    fun deleteHistory(historyId: Long) {
        viewModelScope.launch {
            runCatching {
                repository.deleteHistory(historyId)
            }.onSuccess {
                _uiState.update { state ->
                    if (state.savedHistoryId == historyId) {
                        InjectionTestUiState(statusMessage = "기록을 삭제했어요.")
                    } else {
                        state.copy(statusMessage = "기록을 삭제했어요.")
                    }
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(errorMessage = throwable.message ?: "삭제하지 못했어요.")
                }
            }
        }
    }

    fun showStatusMessage(message: String) {
        _uiState.update { it.copy(statusMessage = message) }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun clearStatusMessage() {
        _uiState.update { it.copy(statusMessage = null) }
    }

    class Factory(private val repository: InjectionRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(InjectionTestViewModel::class.java)) {
                return InjectionTestViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}

private fun levelLabelKo(level: TestLevel): String = when (level) {
    TestLevel.LOW -> "낮음"
    TestLevel.MEDIUM -> "중간"
    TestLevel.HIGH -> "높음"
    TestLevel.ALL -> "전체"
}
