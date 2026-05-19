package com.example.pilab.core.model

import kotlinx.serialization.Serializable

enum class ScenarioId(val wireValue: String) {
    CUSTOMER_SUPPORT_BOT("customer_support_bot"),
    DOCUMENT_SUMMARY_BOT("document_summary_bot"),
    CODE_REVIEW_BOT("code_review_bot")
}

enum class TestLevel(val label: String, val wireValue: String) {
    LOW("Low", "low"),
    MEDIUM("Medium", "medium"),
    HIGH("High", "high"),
    ALL("All Levels", "all")
}

enum class RiskLevel {
    SAFE,
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

enum class TestResultType(val label: String) {
    DEFENSE_SUCCESS("Defense Success"),
    PARTIAL_DEFENSE("Partial Defense"),
    ATTACK_SUCCESS("Attack Success"),
    UNCLEAR("Unclear")
}

data class Scenario(
    val id: ScenarioId,
    val title: String,
    val description: String,
    val role: String,
    val allowedActions: List<String>,
    val blockedActions: List<String>,
    val examplePrompt: String
)

@Serializable
data class InjectionTestResult(
    val finalRiskScore: Int,
    val riskLevel: String,
    val attackTypes: List<String>,
    val levelResults: List<LevelResult>,
    val detailScores: DetailScores
)

@Serializable
data class LevelResult(
    val level: String,
    val result: String,
    val vulnerabilityScore: Int,
    val summary: String
)

@Serializable
data class DetailScores(
    val instructionOverride: Int,
    val roleHijacking: Int,
    val promptLeakage: Int,
    val policyBypass: Int,
    val outputManipulation: Int,
    val modelVulnerability: Int
)

@Serializable
data class SecurityReport(
    val summary: String,
    val attackAnalysis: String,
    val modelComparison: String,
    val recommendations: List<String>
)

data class InjectionHistory(
    val id: Long,
    val scenario: String,
    val prompt: String,
    val selectedLevel: String,
    val result: InjectionTestResult,
    val createdAt: Long
)
