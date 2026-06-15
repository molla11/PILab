package com.example.pilab.core.database

import com.example.pilab.core.model.DetailScores
import com.example.pilab.core.model.InjectionHistory
import com.example.pilab.core.model.InjectionTestResult
import com.example.pilab.core.model.LevelResult
import com.example.pilab.core.model.SecurityReport
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

private val dbJson = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

fun InjectionTestResult.toEntity(
    scenario: String,
    prompt: String,
    selectedLevel: String,
    createdAt: Long = System.currentTimeMillis()
) = InjectionHistoryEntity(
    scenario = scenario,
    prompt = prompt,
    selectedLevel = selectedLevel,
    finalRiskScore = finalRiskScore,
    riskLevel = riskLevel,
    attackTypesJson = dbJson.encodeToString(ListSerializer(String.serializer()), attackTypes),
    detailScoresJson = dbJson.encodeToString(DetailScores.serializer(), detailScores),
    levelResultsJson = dbJson.encodeToString(ListSerializer(LevelResult.serializer()), levelResults),
    createdAt = createdAt
)

fun InjectionHistoryEntity.toDomain() = InjectionHistory(
    id = id,
    scenario = scenario,
    prompt = prompt,
    selectedLevel = selectedLevel,
    result = InjectionTestResult(
        finalRiskScore = finalRiskScore,
        riskLevel = riskLevel,
        attackTypes = decodeOrDefault(
            fallback = listOf("Potential Injection"),
            decode = { dbJson.decodeFromString(ListSerializer(String.serializer()), attackTypesJson) }
        ),
        detailScores = decodeOrDefault(
            fallback = DetailScores(0, 0, 0, 0, 0, finalRiskScore.coerceIn(0, 100)),
            decode = { dbJson.decodeFromString(DetailScores.serializer(), detailScoresJson) }
        ),
        levelResults = decodeOrDefault(
            fallback = listOf(
                LevelResult(
                    level = selectedLevel,
                    result = "Unclear",
                    vulnerabilityScore = finalRiskScore.coerceIn(0, 100),
                    summary = "저장된 상세 단계 결과를 읽지 못해 요약 정보만 표시합니다."
                )
            ),
            decode = { dbJson.decodeFromString(ListSerializer(LevelResult.serializer()), levelResultsJson) }
        )
    ),
    createdAt = createdAt
)

fun SecurityReport.toEntity(historyId: Long, createdAt: Long = System.currentTimeMillis()) = SecurityReportEntity(
    historyId = historyId,
    summary = summary,
    analysis = attackAnalysis,
    modelComparison = modelComparison,
    recommendationJson = dbJson.encodeToString(ListSerializer(String.serializer()), recommendations),
    createdAt = createdAt
)

fun SecurityReportEntity.toDomain() = SecurityReport(
    summary = summary,
    attackAnalysis = analysis,
    modelComparison = modelComparison,
    recommendations = decodeOrDefault(
        fallback = emptyList(),
        decode = { dbJson.decodeFromString(ListSerializer(String.serializer()), recommendationJson) }
    )
)

private inline fun <T> decodeOrDefault(fallback: T, decode: () -> T): T {
    return runCatching(decode).getOrDefault(fallback)
}
