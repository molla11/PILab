package com.example.pilab.core.network

import com.example.pilab.core.model.DetailScores
import com.example.pilab.core.model.InjectionTestResult
import com.example.pilab.core.model.LevelResult
import kotlinx.serialization.Serializable

@Serializable
data class InjectionTestRequestDto(
    val scenario: String,
    val prompt: String,
    val level: String
)

@Serializable
data class InjectionTestResponseDto(
    val finalRiskScore: Int,
    val riskLevel: String,
    val attackTypes: List<String>,
    val levelResults: List<LevelResultDto>,
    val detailScores: DetailScoresDto,
    val analysisSource: String? = null
)

@Serializable
data class LevelResultDto(
    val level: String,
    val result: String,
    val vulnerabilityScore: Int,
    val summary: String
)

@Serializable
data class DetailScoresDto(
    val instructionOverride: Int,
    val roleHijacking: Int,
    val promptLeakage: Int,
    val policyBypass: Int,
    val outputManipulation: Int,
    val modelVulnerability: Int
)

@Serializable
data class SecurityReportRequestDto(
    val scenario: String,
    val prompt: String,
    val result: InjectionTestResponseDto,
    val includeRecommendations: Boolean = true
)

@Serializable
data class SecurityReportResponseDto(
    val summary: String,
    val attackAnalysis: String,
    val modelComparison: String,
    val recommendations: List<String>,
    val analysisSource: String? = null
)

fun InjectionTestResponseDto.toDomain() = InjectionTestResult(
    finalRiskScore = finalRiskScore,
    riskLevel = riskLevel,
    attackTypes = attackTypes,
    levelResults = levelResults.map {
        LevelResult(
            level = it.level,
            result = it.result,
            vulnerabilityScore = it.vulnerabilityScore,
            summary = it.summary
        )
    },
    detailScores = DetailScores(
        instructionOverride = detailScores.instructionOverride,
        roleHijacking = detailScores.roleHijacking,
        promptLeakage = detailScores.promptLeakage,
        policyBypass = detailScores.policyBypass,
        outputManipulation = detailScores.outputManipulation,
        modelVulnerability = detailScores.modelVulnerability
    )
)

fun InjectionTestResult.toDto() = InjectionTestResponseDto(
    finalRiskScore = finalRiskScore,
    riskLevel = riskLevel,
    attackTypes = attackTypes,
    levelResults = levelResults.map {
        LevelResultDto(
            level = it.level,
            result = it.result,
            vulnerabilityScore = it.vulnerabilityScore,
            summary = it.summary
        )
    },
    detailScores = DetailScoresDto(
        instructionOverride = detailScores.instructionOverride,
        roleHijacking = detailScores.roleHijacking,
        promptLeakage = detailScores.promptLeakage,
        policyBypass = detailScores.policyBypass,
        outputManipulation = detailScores.outputManipulation,
        modelVulnerability = detailScores.modelVulnerability
    )
)
