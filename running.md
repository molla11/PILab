# PILab Development Log

## 2026-05-19

- Reviewed `implementation_plan.md` and `plan.md`.
- Confirmed the current project is a basic Android XML/AppCompat skeleton, while the plan requires a Compose-based PILab MVP.
- Implementation direction: convert the Android client to Jetpack Compose, add navigation/state/repository/database/API layers, and provide a usable mock-backed flow when the backend is unavailable.
- User requested this running progress log be accumulated in `running.md`.
- Created initial Git commit before implementation: `9dc7fb5 Initial commit`.
- Next step: update Gradle configuration for Kotlin, Compose, Navigation, Lifecycle, Retrofit, Kotlin Serialization, and Room.
- Updated Gradle configuration for Kotlin, Compose, Navigation, Lifecycle, Retrofit, Kotlin Serialization, Room, KSP, and OkHttp logging.
- Converted `MainActivity` from XML/AppCompat entry to a Compose `ComponentActivity`.
- Added core domain models, built-in scenarios, Retrofit DTOs/API client, Room entities/DAO/database, repository, ViewModel, navigation routes, theme, and Compose screens for the MVP flow.
- Next step: run Gradle build and fix compile/runtime integration issues.
- Fixed AGP 9/KSP integration by disabling built-in Kotlin and new DSL compatibility mode in `gradle.properties`.
- Fixed Kotlin/JVM target mismatch by setting the app module Kotlin toolchain to Java 11.
- Fixed compile errors in serialization imports and Retrofit kotlinx serialization converter import.
- Verified `./gradlew.bat :app:assembleDebug` succeeds. Remaining Gradle output is deprecation warnings from AGP 9 legacy DSL/KSP compatibility mode.
- Next step: run unit tests and review the final worktree.
- Verified `./gradlew.bat :app:testDebugUnitTest` succeeds. The same AGP 9 compatibility-mode deprecation warnings remain.
- Current implementation status: Android MVP client is functional with Compose UI, scenario selection, prompt input, level selection, mock/API-backed test execution, result summary, detail scores, security report generation, Room-backed history, and Retrofit contracts for the planned backend.
- Committed current MVP implementation: `fe575ec Implement Compose PILab MVP`.
- Continuing with polish and contract hardening: move API base URL into `BuildConfig`, improve report persistence behavior, and keep validation passing.
- Moved Android API base URL to `BuildConfig.PILAB_BASE_URL`.
- Changed report generation to auto-save the test result first when there is no existing history id, so generated reports can be persisted and reopened from history.
- Added a NestJS-style backend skeleton under `server/` with `GET /api/health`, `POST /api/injection/test`, and `POST /api/injection/report`.
- Backend currently uses deterministic rule-based analysis matching the Android mock/DTO contract; OpenRouter integration remains a later step.
- Verified Android again after polish: `./gradlew.bat :app:assembleDebug` and `./gradlew.bat :app:testDebugUnitTest` both succeed.
- Installed backend dependencies with `npm.cmd install`; npm reported 0 vulnerabilities.
- Verified backend build with `npm.cmd run build`.
- Added ignore rules for `server/node_modules/` and `server/dist/`.
- Added `server/README.md` with endpoint and run instructions.
- Started client 90% pass at user request.
- Added client-side analysis/report source tracking so the UI can distinguish backend API responses from local mock fallback.
- Prevented duplicate result saves and added status snackbars separate from error snackbars.
- Added history deletion support, settings screen, recent-test direct opening, saved report re-open behavior, and visible client configuration/fallback information.
- Verified the client 90% pass with `./gradlew.bat :app:assembleDebug` and `./gradlew.bat :app:testDebugUnitTest`; both succeed with the existing AGP 9 compatibility warnings.
