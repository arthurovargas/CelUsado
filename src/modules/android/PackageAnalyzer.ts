import NativePackageAnalyzer from './NativePackageAnalyzer';
import {logger} from '../../utils';

export interface PackageAnalysisResult {
  packages: NormalizedPackage[];
  coverage: CoverageInfo;
}

export interface NormalizedPackage {
  packageName: string;
  label: string;
  version: {
    name: string;
    code: number;
  };
  flags: {
    system: boolean;
    updatedSystem: boolean;
    debuggable: boolean;
  };
  discoverySources: string[];
  visibility: string;
  installerPackageName: string | null;
  firstInstallTime: number;
  lastUpdateTime: number;
  classification: {
    category: string;
    confidence: number;
    evidence: string[];
  } | null;
  declaredPermissions: string[];
  services: {
    name: string;
    permission: string | null;
    foregroundServiceType: string | null;
    isExported: boolean;
  }[];
  receivers: {
    name: string;
    permission: string | null;
    isExported: boolean;
  }[];
  activities: {
    name: string;
    permission: string | null;
    isExported: boolean;
  }[];
  providers: {
    name: string;
    authority: string | null;
    isExported: boolean;
    readPermission: string | null;
    writePermission: string | null;
  }[];
  signing: {
    schemeVersion: number;
    hasMultipleSigners: boolean;
    hasPastSigningCertificates: boolean;
    currentSigners: string[];
    historicalSigners: string[];
  } | null;
  indicators: string[];
}

export interface CoverageInfo {
  status: string;
  packagesDiscovered: number;
  packagesAnalyzed: number;
  visibilityLimitations: boolean;
  limitations: string[];
}

const TAG = 'PackageAnalyzer';

export async function analyzeAllPackages(): Promise<PackageAnalysisResult> {
  try {
    logger.info(TAG, 'Starting package analysis from native module');
    const raw = await NativePackageAnalyzer.analyzeAllPackages();
    logger.info(TAG, 'Package analysis completed successfully');
    return raw as unknown as PackageAnalysisResult;
  } catch (error) {
    logger.error(TAG, 'Failed to analyze packages', error);
    throw error;
  }
}
