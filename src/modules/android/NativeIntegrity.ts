import { NativeModules } from 'react-native';

const { AppIntegrity } = NativeModules;

export interface IntegrityEvidenceItem {
  type: string;
  source: string;
  field: string | null;
  value: string | null;
  description: string | null;
}

export interface IntegrityIndicator {
  type: string;
  status: string;
  confidence: number;
  source: string;
  value: string | null;
  evidence: IntegrityEvidenceItem[];
  limitations: string[];
}

export interface IntegrityCoverage {
  status: string;
  availableChecks: string[];
  unavailableChecks: string[];
  limitations: string[];
}

export interface IntegrityResult {
  overallStatus: string;
  indicators: IntegrityIndicator[];
  coverage: IntegrityCoverage;
  limitations: string[];
}

export async function analyzeIntegrity(): Promise<IntegrityResult> {
  try {
    const result = await AppIntegrity.analyzeIntegrity();
    return result as IntegrityResult;
  } catch (error) {
    throw error;
  }
}
