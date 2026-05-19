package com.example.pilab.core.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface InjectionHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: InjectionHistoryEntity): Long

    @Query("SELECT * FROM injection_history ORDER BY createdAt DESC")
    fun observeHistories(): Flow<List<InjectionHistoryEntity>>

    @Query("SELECT * FROM injection_history WHERE id = :id")
    suspend fun getHistoryById(id: Long): InjectionHistoryEntity?

    @Delete
    suspend fun deleteHistory(history: InjectionHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: SecurityReportEntity): Long

    @Query("SELECT * FROM security_report WHERE historyId = :historyId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getReportByHistoryId(historyId: Long): SecurityReportEntity?
}
