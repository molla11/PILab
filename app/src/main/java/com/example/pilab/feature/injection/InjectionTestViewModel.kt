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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableStateFlow
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
                statusMessage = null
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
                statusMessage = null,
                errorMessage = null
            )
        }
    }

    fun loadExamplePrompt() {
        val scenario = _uiState.value.selectedScenario ?: Scenarios.all.first()
        _uiState.update { it.copy(prompt = scenario.examplePrompt, selectedScenario = scenario) }
    }

    fun selectLevel(level: TestLevel) {
        _uiState.update { it.copy(selectedLevel = level) }
    }

    fun runTest(onComplete: () -> Unit) {
        val state = _uiState.value
        val scenario = state.selectedScenario
        if (scenario == null) {
            _uiState.update { it.copy(errorMessage = "Select a scenario first.") }
            return
        }
        if (state.prompt.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Enter a prompt injection sample.") }
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isRunning = true,
                    currentStep = "Preparing request",
                    result = null,
                    report = null,
                    savedHistoryId = null,
                    analysisSource = null,
                    reportSource = null,
                    statusMessage = null,
                    errorMessage = null
                )
            }
            try {
                _uiState.update { it.copy(currentStep = "Running ${state.selectedLevel.label} test") }
                val outcome = repository.runTest(scenario, state.prompt, state.selectedLevel)
                _uiState.update {
                    it.copy(
                        isRunning = false,
                        currentStep = "Analysis complete",
                        result = outcome.result,
                        analysisSource = outcome.source,
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
                        errorMessage = exception.message ?: "Test failed."
                    )
                }
            }
        }
    }

    fun saveResult() {
        val state = _uiState.value
        val scenario = state.selectedScenario
        val result = state.result
        if (scenario == null || result == null) {
            _uiState.update { it.copy(errorMessage = "No result is available to save.") }
            return
        }
        if (state.savedHistoryId != null) {
            _uiState.update { it.copy(statusMessage = "Result is already saved.") }
            return
        }
        viewModelScope.launch {
            runCatching {
                repository.saveResult(scenario, state.prompt, state.selectedLevel, result)
            }.onSuccess { historyId ->
                _uiState.update { it.copy(savedHistoryId = historyId, statusMessage = "Result saved to history.") }
            }.onFailure { throwable ->
                _uiState.update { it.copy(errorMessage = throwable.message ?: "Could not save result.") }
            }
        }
    }

    fun generateReport(onComplete: () -> Unit) {
        val state = _uiState.value
        val scenario = state.selectedScenario
        val result = state.result
        if (scenario == null || result == null) {
            _uiState.update { it.copy(errorMessage = "Run a test before generating a report.") }
            return
        }
        if (state.report != null) {
            onComplete()
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isRunning = true, currentStep = "Generating report", errorMessage = null) }
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
                        currentStep = "Report ready",
                        report = outcome.report,
                        savedHistoryId = historyId,
                        reportSource = outcome.source,
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
                        errorMessage = exception.message ?: "Could not generate report."
                    )
                }
            }
        }
    }

    fun loadHistory(historyId: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            val history = repository.getHistory(historyId)
            if (history == null) {
                _uiState.update { it.copy(errorMessage = "History item not found.") }
                return@launch
            }
            val scenario = Scenarios.find(history.scenario) ?: Scenarios.all.first()
            val report = repository.getSavedReport(historyId)
            _uiState.update {
                it.copy(
                    selectedScenario = scenario,
                    prompt = history.prompt,
                    selectedLevel = TestLevel.entries.firstOrNull { level -> level.wireValue == history.selectedLevel } ?: TestLevel.ALL,
                    result = history.result,
                    report = report,
                    savedHistoryId = history.id,
                    analysisSource = null,
                    reportSource = if (report == null) null else AnalysisSource.API,
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
                        InjectionTestUiState(statusMessage = "History item deleted.")
                    } else {
                        state.copy(statusMessage = "History item deleted.")
                    }
                }
            }.onFailure { throwable ->
                _uiState.update { it.copy(errorMessage = throwable.message ?: "Could not delete history item.") }
            }
        }
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
