export type TestLevel = 'low' | 'medium' | 'high' | 'all';

export interface InjectionTestRequestDto {
  scenario: string;
  prompt: string;
  level: TestLevel;
}

export interface LevelResultDto {
  level: string;
  result: string;
  vulnerabilityScore: number;
  summary: string;
}

export interface DetailScoresDto {
  instructionOverride: number;
  roleHijacking: number;
  promptLeakage: number;
  policyBypass: number;
  outputManipulation: number;
  modelVulnerability: number;
}

export interface InjectionTestResponseDto {
  finalRiskScore: number;
  riskLevel: string;
  attackTypes: string[];
  levelResults: LevelResultDto[];
  detailScores: DetailScoresDto;
  analysisSource?: 'openrouter' | 'server_fallback';
}

export interface SecurityReportRequestDto {
  scenario: string;
  prompt: string;
  result: InjectionTestResponseDto;
  includeRecommendations?: boolean;
}

export interface SecurityReportResponseDto {
  summary: string;
  attackAnalysis: string;
  modelComparison: string;
  recommendations: string[];
  analysisSource?: 'openrouter' | 'server_fallback';
}
