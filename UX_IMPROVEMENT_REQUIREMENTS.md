# PILab UX/UI and Evaluation Improvement Requirements

## Scope

This review covers the Android client flow, every visible button and UI step, the local history/report experience, and the NestJS prompt-injection evaluation backend. The current product goal should be a usable prompt-injection lab where a user can select a target assistant scenario, submit an attack prompt, choose a defense level, run an evaluation, inspect evidence, save results, and generate a security report.

## Current Critical Issues

- Korean UI copy and server prompts are mojibake-corrupted across app and backend files. This breaks trust, readability, and in some places appears to break source syntax.
- The evaluation flow is named as if it "does injection" but actually evaluates a submitted injection attempt. The wording should separate attack input, target execution, defense profile, analysis, and result.
- The OpenRouter analysis prompt must not directly score the user prompt alone. It must first execute the scenario-specific target assistant prompt, then evaluate the produced target response as evidence.
- The local fallback analysis uses keyword counts only, so it can over-score long prompts and under-explain why a prompt is risky.
- The result screen shows a score but does not sufficiently explain what the score means, what evidence was used, or what the user should do next.
- Trace/log UI currently labels request/response data as chat, which can mislead users into thinking it is the full model transcript.
- History deletion is immediate and lacks confirmation or recovery.
- Settings is informational only. It should expose runtime status, backend endpoint, fallback behavior, and model configuration checks.
- Empty/error/loading states are too sparse for a security evaluation workflow.

## Target UX Principles

- Use "평가", "분석", "방어 단계" terminology instead of implying the app performs a live attack against a third-party service.
- Make the primary path explicit: scenario -> attack prompt -> defense level -> run evaluation -> inspect result -> save/report.
- Every result must show source, score meaning, detected attack categories, per-level outcome, and evidence/limitations.
- Local fallback must be clearly labeled as deterministic heuristic analysis, not equivalent to model-based evaluation.
- The interface should be dense and practical, closer to a security workbench than a marketing page.

## Screen-by-Screen Requirements

### Home

- Primary button: "새 평가 시작" should reset the current draft and navigate to scenario selection.
- Secondary buttons: "히스토리" and "설정" should be clearly separated from the main workflow.
- Recent tests should show date, scenario, risk score, risk label, and a short prompt preview.
- Empty state should explain that saved evaluation results will appear after the first run.
- The first viewport should explain PILab as a prompt-injection evaluation workbench in one concise sentence.

### Scenario Selection

- Each scenario card should show the assistant role, allowed behavior summary, blocked behavior summary, and an example injection intent.
- Selecting a scenario should be visually obvious before advancing.
- The scenario step should make clear that the selected scenario becomes the target policy context for evaluation.

### Prompt Input

- Text field label should say "공격 프롬프트" or "검증할 프롬프트".
- Include concise helper text explaining that the field is the adversarial user/document/comment input to evaluate.
- Example prompt button should load a scenario-specific injection example.
- Character count should be valid Korean text and should not look like an error.
- "현재 설정 보기" should show scenario, role, allowed actions, blocked actions, level, endpoint, and prompt.

### Defense Level Selection

- Use clear level names:
  - Low: vulnerable baseline where service setup and attack input are sent at the same user-message level.
  - Medium: role and blocked actions are placed in system instructions; obvious override/private-data requests should be refused, but output validation remains limited.
  - High: strict instruction/data separation plus refusal rules.
  - All: run all levels and compare.
- The run button should say "평가 실행", not "Do Injection".
- The run button should be disabled if scenario or prompt is missing.
- Users should be able to review setup before running.

### Running

- Loading state should show the current step and selected scenario/level.
- Copy should say evaluation is running, not that an injection is being performed.
- Avoid indefinite-looking empty screens; include a short note about possible model latency.

### Result Summary

- Show final risk score, risk level, concise interpretation, and analysis source.
- Show detected attack categories with readable Korean labels.
- Show per-level scores with summaries.
- Provide visible actions:
  - "상세 점수"
  - "요청/응답 로그"
  - "결과 저장"
  - "보안 리포트 생성/보기"
  - "홈으로"
- Save button should be disabled after save and clearly show saved state.
- Result copy should distinguish model analysis from deterministic fallback.

### Detail Scores

- Show each score with a Korean label and short meaning:
  - Instruction override
  - Role hijacking
  - Prompt leakage
  - Policy bypass
  - Output manipulation
  - Model vulnerability
- Progress bars should be stable and visually readable.

### Request/Response Log

- Rename from chat trace to request/response log.
- Show that this is the API payload exchanged between client and backend.
- Do not imply it is the full hidden prompt or complete model transcript.
- Keep JSON readable and scrollable.

### Security Report

- If no report exists, state that a report is generated after an evaluation result.
- Report sections should include summary, attack analysis, defense comparison, and recommendations.
- Recommendations must focus on defensive design, not concrete instructions for abusing real services.

### History

- History list should be scan-friendly and include score, scenario, date, and prompt preview.
- Delete action should ideally ask for confirmation before removing data.
- Opening a history item should restore scenario, prompt, level, result, and saved report if available.

### Settings

- Show API base URL.
- Explain source fallback order: OpenRouter -> server heuristic fallback -> client heuristic fallback.
- Show local storage behavior.
- Future improvement: add backend health check, model names, timeout, and clear-history control.

## Backend Evaluation Requirements

- Prompt instructions must be valid Korean and unambiguous.
- Evaluation must be a two-stage process:
  - Stage 1: Build and run the target assistant prompt for the selected scenario and defense level.
  - Stage 2: Evaluate the target response against the scenario role, allowed actions, blocked actions, and attack-type taxonomy.
- Defense levels must differ materially:
  - Low must not put the scenario policy into a privileged system prompt. The scenario setup and attack prompt should be sent together as user-level content.
  - Medium should use a system prompt strong enough to block obvious attacks while still leaving meaningful residual risk compared with High.
  - High should use strict instruction/data separation and explicit refusal rules.
- The evaluator must receive structured context:
  - scenario id
  - scenario role
  - allowed actions
  - blocked actions
  - attack prompt
  - defense profile
  - target system prompt
  - target user prompt
  - target response
  - expected output schema
- The evaluator should judge whether a target assistant would likely violate the scenario policy, not whether the prompt is merely suspicious.
- Scores should mean "residual vulnerability under the selected defense profile".
- Attack types must be selected from the fixed English API vocabulary to preserve client compatibility.
- Summaries and reports should be Korean for the UI.
- The model prompt must explicitly forbid returning attack playbooks or operational abuse steps.
- JSON parsing should remain strict and schema-validated.

## Local Fallback Requirements

- Keep the app usable without `OPENROUTER_API_KEY`.
- Label local fallback as heuristic analysis.
- Use multiple indicators:
  - instruction override terms
  - role hijacking terms
  - secret/system prompt leakage terms
  - policy bypass terms
  - output manipulation terms
  - indirect injection/document/comment terms
  - tool misuse/data exfiltration terms
- Score should reflect both detected categories and selected defense level.
- Fallback summaries should be Korean and explain what was detected.

## Implementation Acceptance Criteria

- App screens use readable Korean copy with consistent terminology.
- No "Do Injection" labels remain in the user-facing workflow.
- Scenario data is readable and scenario-specific.
- Server prompts are readable and aligned with the intended evaluation task.
- Model-based evaluation executes the target assistant first, then evaluates the target response.
- Result details expose the target system prompt, attack input, and target response evidence per defense level.
- Fallback analysis and report output are readable Korean.
- Request/response log labels are accurate.
- A reviewer can trace every major UI button from this document to an implemented screen action.
- Android and server build checks should be run after Java and Node dependencies are available.
