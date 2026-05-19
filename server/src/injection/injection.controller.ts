import { Body, Controller, Post } from '@nestjs/common';
import { InjectionService } from './injection.service';
import {
  InjectionTestRequestDto,
  SecurityReportRequestDto
} from './dto';

@Controller('api/injection')
export class InjectionController {
  constructor(private readonly injectionService: InjectionService) {}

  @Post('test')
  runInjectionTest(@Body() request: InjectionTestRequestDto) {
    return this.injectionService.runInjectionTest(request);
  }

  @Post('report')
  generateReport(@Body() request: SecurityReportRequestDto) {
    return this.injectionService.generateReport(request);
  }
}
