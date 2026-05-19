# PILab Implementation Plan

## 1. 구현 목표

`plan.md`의 제안서를 실제 개발 작업으로 전환하기 위한 구현 계획이다. 우선순위는 Android 클라이언트 MVP를 빠르게 세우고, 이후 NestJS 백엔드와 OpenRouter 연동을 붙이는 순서로 둔다.

MVP 완료 기준은 다음과 같다.

1. 사용자가 시나리오를 선택한다.
2. 프롬프트 인젝션 입력을 작성한다.
3. Low / Medium / High / All Levels 중 테스트 레벨을 선택한다.
4. 백엔드 API로 테스트 요청을 보낸다.
5. 결과 요약, 세부 점수, 상세 리포트를 화면에서 확인한다.
6. 테스트 결과와 리포트를 Room DB에 저장하고 History에서 다시 조회한다.

## 2. 전체 구현 순서

### Phase 1. Android 프로젝트 생성 및 기본 뼈대

목표: 바로 화면 개발이 가능한 Compose 기반 Android 프로젝트를 만든다.

작업 항목:

1. Android Studio 프로젝트 생성
    - 언어: Kotlin
    - UI: Jetpack Compose
    - 최소 SDK: 26 이상 권장
    - 앱 이름: `PILab`
    - 패키지 예시: `com.pilab.app`
2. Gradle 의존성 추가
    - Compose Material 3
    - Navigation Compose
    - Lifecycle ViewModel Compose
    - Kotlin Coroutines
    - Retrofit
    - OkHttp Logging Interceptor
    - Kotlin Serialization 또는 Moshi
    - Room
3. 기본 패키지 구조 생성
    - `core/design`
    - `core/network`
    - `core/database`
    - `core/model`
    - `feature/home`
    - `feature/injection`
    - `feature/result`
    - `feature/history`
    - `feature/report`
4. 앱 테마 구성
    - Material 3 기반
    - 보안 실습 앱에 맞는 차분한 색상 체계
    - 점수/위험도 표시에 사용할 상태 색상 정의

완료 기준:

- 앱이 빌드되고 기본 Home 화면이 표시된다.
- Navigation Host가 구성되어 빈 화면 간 이동이 가능하다.

### Phase 2. 클라이언트 도메인 모델 정의

목표: UI, API, DB가 공통으로 사용할 핵심 타입을 먼저 고정한다.

주요 모델:

```kotlin
enum class ScenarioId {
    CUSTOMER_SUPPORT_BOT,
    DOCUMENT_SUMMARY_BOT,
    CODE_REVIEW_BOT
}

enum class TestLevel {
    LOW,
    MEDIUM,
    HIGH,
    ALL
}

enum class RiskLevel {
    SAFE,
    LOW,
    MEDIUM,
    HIGH,
    CRITICAL
}

enum class TestResultType {
    DEFENSE_SUCCESS,
    PARTIAL_DEFENSE,
    ATTACK_SUCCESS,
    UNCLEAR
}
```

데이터 클래스:

```kotlin
data class Scenario(
    val id: ScenarioId,
    val title: String,
    val description: String,
    val role: String,
    val allowedActions: List<String>,
    val blockedActions: List<String>
)

data class InjectionTestRequest(
    val scenario: String,
    val prompt: String,
    val level: String
)

data class InjectionTestResult(
    val finalRiskScore: Int,
    val riskLevel: String,
    val attackTypes: List<String>,
    val levelResults: List<LevelResult>,
    val detailScores: DetailScores
)

data class LevelResult(
    val level: String,
    val result: String,
    val vulnerabilityScore: Int,
    val summary: String
)

data class DetailScores(
    val instructionOverride: Int,
    val roleHijacking: Int,
    val promptLeakage: Int,
    val policyBypass: Int,
    val outputManipulation: Int,
    val modelVulnerability: Int
)
```

완료 기준:

- 화면, 네트워크, DB에서 사용할 타입 이름과 필드가 일관된다.
- 서버 API 응답 예시와 매핑 가능한 DTO가 존재한다.

### Phase 3. Navigation 및 화면 흐름 구현

목표: `plan.md`의 앱 흐름도를 실제 클라이언트 라우팅으로 구현한다.

화면 목록:

1. `HomeScreen`
    - 앱 이름 `PILab`
    - `Injection Test`, `History`, `Settings` 진입 버튼
    - 최근 테스트 요약 영역
2. `ScenarioSelectScreen`
    - 고객 상담 챗봇
    - 문서 요약 봇
    - 코드 리뷰 봇
    - 선택 후 다음 이동
3. `PromptInputScreen`
    - 선택된 시나리오 표시
    - 프롬프트 입력창
    - 예시 프롬프트 불러오기
    - 입력 길이 표시
4. `LevelSelectScreen`
    - Low / Medium / High / All Levels 선택
    - 실행 버튼
5. `RunningTestScreen`
    - 현재 요청 상태
    - 단계별 진행 표시
    - API 호출 중 로딩 UI
6. `ResultSummaryScreen`
    - 최종 위험도 점수
    - 위험 등급
    - 공격 유형 태그
    - 레벨별 결과 요약
    - 세부 점수, 리포트, 저장 버튼
7. `DetailScoresScreen`
    - 항목별 점수
    - Progress bar
    - 설명 문구
8. `SecurityReportScreen`
    - 요약
    - 공격 분석
    - 모델 비교
    - 방어 제안
9. `HistoryScreen`
    - 저장된 테스트 목록
    - 상세 결과 재조회

Navigation route 예시:

```kotlin
sealed class PilabRoute(val route: String) {
    data object Home : PilabRoute("home")
    data object ScenarioSelect : PilabRoute("scenario-select")
    data object PromptInput : PilabRoute("prompt-input")
    data object LevelSelect : PilabRoute("level-select")
    data object RunningTest : PilabRoute("running-test")
    data object ResultSummary : PilabRoute("result-summary")
    data object DetailScores : PilabRoute("detail-scores")
    data object SecurityReport : PilabRoute("security-report")
    data object History : PilabRoute("history")
}
```

완료 기준:

- 사용자가 Home에서 테스트 실행 흐름을 끝까지 이동할 수 있다.
- 실제 API가 없어도 더미 데이터로 결과 화면까지 확인할 수 있다.

### Phase 4. Injection Test 상태 관리

목표: 테스트 진행에 필요한 화면 상태를 하나의 ViewModel에서 안정적으로 관리한다.

구성:

- `InjectionTestViewModel`
- `InjectionTestUiState`
- `InjectionRepository`

상태 필드:

```kotlin
data class InjectionTestUiState(
    val selectedScenario: Scenario? = null,
    val prompt: String = "",
    val selectedLevel: TestLevel = TestLevel.ALL,
    val isRunning: Boolean = false,
    val currentStep: String? = null,
    val result: InjectionTestResult? = null,
    val errorMessage: String? = null
)
```

주요 액션:

- `selectScenario(scenario: Scenario)`
- `updatePrompt(prompt: String)`
- `loadExamplePrompt()`
- `selectLevel(level: TestLevel)`
- `runTest()`
- `clearError()`
- `saveResult()`

완료 기준:

- 화면 간 이동 후에도 선택 시나리오, 입력 프롬프트, 테스트 레벨이 유지된다.
- 네트워크 성공, 실패, 로딩 상태가 UI에 반영된다.

### Phase 5. API 클라이언트 연동

목표: 백엔드 API 계약에 맞춰 Retrofit 통신을 붙인다.

API:

```http
POST /api/injection/test
POST /api/injection/report
```

Retrofit 인터페이스:

```kotlin
interface InjectionApi {
    @POST("api/injection/test")
    suspend fun runInjectionTest(
        @Body request: InjectionTestRequestDto
    ): InjectionTestResponseDto

    @POST("api/injection/report")
    suspend fun generateReport(
        @Body request: SecurityReportRequestDto
    ): SecurityReportResponseDto
}
```

클라이언트 설정:

- `BASE_URL`은 `BuildConfig` 또는 `local.properties` 기반으로 분리한다.
- Android Emulator에서 로컬 서버 접근 시 기본값은 `http://10.0.2.2:3000/`로 둔다.
- OkHttp timeout은 LLM 응답 시간을 고려해 60초 이상으로 둔다.
- 실패 시 사용자에게 재시도 가능한 에러 메시지를 표시한다.

완료 기준:

- 실제 서버 또는 mock 서버로 `/api/injection/test` 요청을 보내고 결과를 화면에 렌더링한다.
- 서버 연결 실패 시 앱이 죽지 않고 에러 상태를 표시한다.

### Phase 6. Room 히스토리 저장

목표: 테스트 결과와 리포트를 로컬에 저장하고 다시 조회할 수 있게 한다.

Entity:

```kotlin
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
    val recommendationJson: String,
    val createdAt: Long
)
```

DAO:

- `insertHistory`
- `observeHistories`
- `getHistoryById`
- `deleteHistory`
- `insertReport`
- `getReportByHistoryId`

완료 기준:

- Result 화면에서 저장한 결과가 History 화면에 나타난다.
- 앱 재실행 후에도 저장된 기록이 유지된다.

### Phase 7. 상세 리포트 기능

목표: 테스트 결과 기반의 상세 리포트를 생성하고 저장한다.

흐름:

1. Result 화면에서 `Generate Security Report` 클릭
2. `/api/injection/report` 요청
3. 응답을 `SecurityReportScreen`에 표시
4. 사용자가 저장하면 `SecurityReportEntity`로 저장

MVP에서 `testId` 처리 방식:

- 서버 저장이 없는 구조이므로 클라이언트의 `historyId`를 그대로 서버에 의존하지 않는다.
- 리포트 API는 가능하면 테스트 결과 전문을 포함하도록 서버 설계를 조정한다.
- 기존 제안서 API를 유지해야 하면, 클라이언트에서는 결과 저장 후 생성된 `historyId`를 `testId`로 보낸다.

권장 요청 DTO:

```kotlin
data class SecurityReportRequestDto(
    val scenario: String,
    val prompt: String,
    val result: InjectionTestResponseDto,
    val includeRecommendations: Boolean = true
)
```

완료 기준:

- 리포트 생성 API 응답을 화면에 표시한다.
- 생성된 리포트를 History 상세에서 다시 볼 수 있다.

## 3. 백엔드 구현 계획

클라이언트 개발 직후 붙일 서버 구현 계획이다.

### Phase 8. NestJS 서버 기본 구조

모듈:

- `InjectionModule`
- `OpenRouterModule`
- `ScenarioModule`
- `AnalysisModule`
- `ReportModule`

엔드포인트:

```http
POST /api/injection/test
POST /api/injection/report
GET /api/health
```

환경 변수:

```env
OPENROUTER_API_KEY=
OPENROUTER_BASE_URL=https://openrouter.ai/api/v1
LOW_MODEL=
MEDIUM_MODEL=
HIGH_MODEL=
PORT=3000
```

완료 기준:

- Android 클라이언트에서 health check와 test API 호출이 가능하다.
- API Key는 서버 환경 변수에만 존재한다.

### Phase 9. 시나리오 및 프롬프트 빌더

서버는 시나리오별 시스템 프롬프트를 가진다.

시나리오:

- `customer_support_bot`
- `document_summary_bot`
- `code_review_bot`

레벨별 방어 차이:

- Low: 기본 역할 프롬프트만 적용
- Medium: 금지 행동과 안전 규칙 추가
- High: 사용자 입력을 데이터로만 처리하도록 강화하고 응답 후 분석 적용

완료 기준:

- 같은 사용자 입력이라도 레벨별로 다른 시스템 프롬프트가 구성된다.

### Phase 10. 분석 및 점수화

MVP는 LLM 분석과 규칙 기반 분석을 혼합한다.

공격 유형 탐지:

- `Instruction Override`
- `Role Hijacking`
- `System Prompt Leakage`
- `Policy Bypass`
- `Output Manipulation`

점수 산정:

- 입력 프롬프트 위험도
- 모델 응답 취약도
- 공격 유형 개수
- 레벨별 공격 성공 여부
- 시스템 정보 유출 여부

완료 기준:

- API 응답이 `plan.md`의 Response 구조와 호환된다.
- 모든 점수는 0~100 범위로 정규화된다.

## 4. 클라이언트 개발 첫 작업 순서

이 문서를 작성한 직후 바로 착수할 클라이언트 작업 순서다.

1. Android Compose 프로젝트 생성
2. Gradle 의존성 정리
3. 패키지 구조 생성
4. Theme, 색상, Typography 기본값 구성
5. Navigation Host 구성
6. Home 화면 구현
7. ScenarioSelect 화면 구현
8. PromptInput 화면 구현
9. LevelSelect 화면 구현
10. 더미 Repository로 RunningTest 및 ResultSummary 화면 연결
11. DetailScores 화면 구현
12. SecurityReport 화면 구현
13. Retrofit DTO와 API 인터페이스 추가
14. Mock 응답에서 실제 API 호출로 교체
15. Room Entity, DAO, Database 추가
16. History 화면 구현

## 5. 우선순위와 제외 범위

MVP 우선순위:

1. 테스트 플로우 완성
2. 결과 화면 가독성
3. API 계약 안정화
4. 히스토리 저장
5. 리포트 생성

MVP 제외:

- Learning Mode
- 방어 전/후 비교
- PDF 저장
- 커스텀 시나리오 생성
- 통계 대시보드
- Supabase 클라우드 저장
- 간접 프롬프트 인젝션 전용 모드

## 6. 구현 시 주의사항

1. OpenRouter API Key는 Android 클라이언트에 넣지 않는다.
2. Android 앱은 서버 API만 호출한다.
3. 사용자의 프롬프트 기록은 기본적으로 로컬 Room DB에 저장한다.
4. 공격 실습 앱이므로 실제 서비스 공격을 유도하는 문구보다 분석과 방어 설명을 중심에 둔다.
5. API 실패, 타임아웃, 빈 응답, 점수 누락에 대한 fallback UI를 만든다.
6. `All Levels` 요청은 서버에서 Low, Medium, High를 순차 실행하고 한 번에 결과를 반환하는 방식을 기본값으로 둔다.
7. DTO와 도메인 모델은 분리해 서버 응답 변경이 UI 전체로 번지지 않게 한다.

## 7. 검증 계획

클라이언트 검증:

- Compose Preview로 주요 화면 확인
- Emulator에서 전체 플로우 수동 테스트
- 더미 데이터로 결과 화면 렌더링 확인
- 네트워크 실패 시 에러 UI 확인
- Room 저장 후 앱 재실행 테스트

백엔드 검증:

- `GET /api/health` 확인
- `POST /api/injection/test` 샘플 요청 확인
- Low / Medium / High 결과 필드 누락 여부 확인
- OpenRouter API Key 미설정 시 명확한 에러 반환 확인

통합 검증:

- Android Emulator에서 `10.0.2.2:3000` 서버 호출
- All Levels 테스트 실행
- 결과 저장
- History 재조회
- 리포트 생성 및 저장

## 8. 예상 산출물

클라이언트:

- Compose 기반 Android 앱
- MVVM 구조
- Retrofit API 클라이언트
- Room 히스토리 DB
- Home, Test Flow, Result, Report, History 화면

서버:

- NestJS REST API
- OpenRouter 연동
- 시나리오별 프롬프트 빌더
- 레벨별 모델 호출
- 응답 분석 및 점수화
- 리포트 생성 API

문서:

- `implementation_plan.md`
- API 요청/응답 예시
- 개발 및 실행 방법 문서
