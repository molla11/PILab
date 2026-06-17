import { Controller, Get } from '@nestjs/common';
import { createAgent } from './agent/agent';

@Controller('api/health')
export class HealthController {
  @Get()
  health() {
    return {
      status: 'ok',
      service: 'pilab-server',
      openRouterConfigured: Boolean(process.env.OPENROUTER_API_KEY),
      models: {
        low: process.env.LOW_MODEL || 'openrouter/auto',
        medium: process.env.MEDIUM_MODEL || 'openrouter/auto',
        high: process.env.HIGH_MODEL || 'openrouter/auto',
        analyzer: process.env.ANALYZER_MODEL || 'openrouter/auto',
        report: process.env.REPORT_MODEL || 'openrouter/auto'
      }
    };
  }

  @Get('openrouter')
  async openRouterHealth() {
    const apiKey = process.env.OPENROUTER_API_KEY;
    const model = process.env.ANALYZER_MODEL || process.env.MEDIUM_MODEL || 'openrouter/auto';
    const startedAt = Date.now();

    if (!apiKey) {
      return {
        ok: false,
        openRouterConfigured: false,
        model,
        durationMs: Date.now() - startedAt,
        error: 'OPENROUTER_API_KEY is missing'
      };
    }

    try {
      const agent = createAgent({
        apiKey,
        model,
        instructions: 'Return only this exact JSON object and no markdown: {"ok":true}',
        maxSteps: 2
      });
      const raw = await agent.sendSync('Return {"ok":true} exactly.');
      return {
        ok: true,
        openRouterConfigured: true,
        model,
        durationMs: Date.now() - startedAt,
        responsePreview: raw.slice(0, 300)
      };
    } catch (error) {
      return {
        ok: false,
        openRouterConfigured: true,
        model,
        durationMs: Date.now() - startedAt,
        error: sanitizeOpenRouterError(error, apiKey)
      };
    }
  }
}

function sanitizeOpenRouterError(error: unknown, apiKey: string): string {
  const message = error instanceof Error ? error.stack || error.message : String(error);
  return message
    .replaceAll(apiKey, '[redacted]')
    .replace(/sk-or-v1-[A-Za-z0-9]+/g, '[redacted]');
}
