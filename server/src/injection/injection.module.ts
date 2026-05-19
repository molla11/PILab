import { Module } from '@nestjs/common';
import { InjectionController } from './injection.controller';
import { InjectionService } from './injection.service';

@Module({
  controllers: [InjectionController],
  providers: [InjectionService]
})
export class InjectionModule {}
