import { Module } from '@nestjs/common';
import { HealthController } from './health.controller';
import { InjectionModule } from './injection/injection.module';

@Module({
  imports: [InjectionModule],
  controllers: [HealthController]
})
export class AppModule {}
