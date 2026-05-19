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
LOW_MODEL=openrouter/auto
MEDIUM_MODEL=openrouter/auto
HIGH_MODEL=openrouter/auto
ANALYZER_MODEL=openrouter/auto
REPORT_MODEL=openrouter/auto
```

Leave model values blank to use `openrouter/auto`.
