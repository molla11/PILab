# PILab Server

Minimal NestJS backend for the Android PILab client.

## Endpoints

- `GET /api/health`
- `POST /api/injection/test`
- `POST /api/injection/report`

The implementation uses an OpenRouter Agent when `OPENROUTER_API_KEY` is set. If the key is missing or the model response cannot be parsed, it falls back to deterministic rule-based analysis so the Android app remains usable.

## Run

```bash
npm install
npm run start:dev
```

The Android client defaults to `http://10.0.2.2:3000/`, which maps to the host machine from the Android emulator.

## OpenRouter

Create `server/.env` from `.env.example` and set:

```env
OPENROUTER_API_KEY=sk-or-...
LOW_MODEL=google/gemini-3.1-flash-lite
MEDIUM_MODEL=qwen/qwen3.6-flash
HIGH_MODEL=x-ai/grok-4.3
ANALYZER_MODEL=qwen/qwen3.6-flash
REPORT_MODEL=qwen/qwen3.6-max-preview
```

Recommended model split:

- `LOW_MODEL`: low-cost, fast model for lightweight defense evaluation.
- `MEDIUM_MODEL`: Qwen flash-tier model for balanced analysis at low cost.
- `HIGH_MODEL`: Grok 4.3 for stronger security judgment without GPT/Claude pricing.
- `ANALYZER_MODEL`: Qwen flash-tier model for low-cost attack-type classification.
- `REPORT_MODEL`: Qwen max preview for higher-quality report writing without GPT/Claude.

Leave model values blank in the runtime environment to use `openrouter/auto`.
