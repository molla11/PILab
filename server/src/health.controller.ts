import { Controller, Get } from '@nestjs/common';

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
}
