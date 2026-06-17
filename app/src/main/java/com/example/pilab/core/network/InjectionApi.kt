package com.example.pilab.core.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface InjectionApi {
    @GET("api/health")
    suspend fun getHealth(): HealthResponseDto

    @POST("api/injection/test")
    suspend fun runInjectionTest(
        @Body request: InjectionTestRequestDto
    ): InjectionTestResponseDto

    @POST("api/injection/report")
    suspend fun generateReport(
        @Body request: SecurityReportRequestDto
    ): SecurityReportResponseDto
}
