import 'dotenv/config';
import { Logger } from '@nestjs/common';
import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module';

type HttpRequestForLog = {
  method?: string;
  originalUrl?: string;
  url?: string;
};

type HttpResponseForLog = {
  statusCode?: number;
  on(event: 'finish', listener: () => void): void;
};

async function bootstrap() {
  const app = await NestFactory.create(AppModule);
  const logger = new Logger('HTTP');
  app.use((request: HttpRequestForLog, response: HttpResponseForLog, next: () => void) => {
    const startedAt = Date.now();
    response.on('finish', () => {
      const durationMs = Date.now() - startedAt;
      logger.log(`${request.method ?? 'UNKNOWN'} ${request.originalUrl ?? request.url ?? '-'} ${response.statusCode ?? '-'} ${durationMs}ms`);
    });
    next();
  });
  app.enableCors();
  const port = Number(process.env.PORT ?? 3000);
  await app.listen(port);
  new Logger('Bootstrap').log(`PILab server listening on port ${port}`);
}

void bootstrap();
