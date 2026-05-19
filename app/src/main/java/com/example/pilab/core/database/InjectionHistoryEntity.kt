package com.example.pilab.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "injection_history")
data class InjectionHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val scenario: String,
    val prompt: String,
    val selectedLevel: String,
    val finalRiskScore: Int,
    val riskLevel: String,
    val attackTypesJson: String,
    val detailScoresJson: String,
    val levelResultsJson: String,
    val createdAt: Long
)

@Entity(tableName = "security_report")
data class SecurityReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val historyId: Long,
    val summary: String,
    val analysis: String,
    val modelComparison: String,
    val recommendationJson: String,
    val createdAt: Long
)
