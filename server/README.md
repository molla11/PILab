# PILab Server

Minimal NestJS backend for the Android PILab client.

## Endpoints

- `GET /api/health`
- `POST /api/injection/test`
- `POST /api/injection/report`

The current implementation uses deterministic rule-based analysis so the Android app can run end-to-end before OpenRouter integration is added.

## Run

```bash
npm install
npm run start:dev
```

The Android client defaults to `http://10.0.2.2:3000/`, which maps to the host machine from the Android emulator.

