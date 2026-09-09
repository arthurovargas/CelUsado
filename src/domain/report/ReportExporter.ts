import type {PackageAnalysisResult} from '../../modules/android/PackageAnalyzer';
import type {DeviceControlResult} from '../../modules/android/DeviceControl';
import type {IntegrityResult} from '../../modules/android/NativeIntegrity';
import type {RiskResult} from '../rules';
import type {DeviceInfoExtended} from '../device/DeviceInfoService';

export interface DiagnosticReport {
  metadata: {
    version: string;
    appVersion: string;
    rulesVersion: string;
    generatedAt: string;
    deviceFingerprint: string;
  };
  device: {
    manufacturer: string;
    model: string;
    brand: string;
    androidVersion: string;
    sdkVersion: number;
    hardware: string;
    board: string;
    totalRam: number;
    totalStorage: number;
    processorCount: number;
    buildDisplay: string;
    securityPatch: string;
    fingerprint: string;
    bootloader: string;
    buildTags: string;
    buildType: string;
  } | null;
  risk: {
    score: number;
    level: string;
    recommendation: string;
    triggeredRules: {
      ruleId: string;
      name: string;
      category: string;
      severity: string;
      evidence: string;
      source: string;
    }[];
  } | null;
  packages: {
    total: number;
    byCategory: Record<string, number>;
    items: {
      packageName: string;
      name: string;
      classification: string;
      isSystemApp: boolean;
      indicators: string[];
    }[];
  };
  adminIndicators: {
    type: string;
    packageName: string;
    status: string;
    confidence: number;
  }[];
  integrity: {
    overallStatus: string;
    indicators: {
      type: string;
      status: string;
      confidence: number;
      source: string;
      value: string | null;
    }[];
  } | null;
}

const APP_VERSION = '0.0.1';
const RULES_VERSION = '1.0.0';

export function generateReport(
  deviceInfo: DeviceInfoExtended | null,
  packages: PackageAnalysisResult,
  control: DeviceControlResult,
  integrity: IntegrityResult,
  risk: RiskResult | null,
): DiagnosticReport {
  const now = new Date();

  const packageStats: Record<string, number> = {};
  for (const pkg of packages.packages) {
    const cat = pkg.classification?.category || 'UNKNOWN';
    packageStats[cat] = (packageStats[cat] || 0) + 1;
  }

  return {
    metadata: {
      version: '1.0.0',
      appVersion: APP_VERSION,
      rulesVersion: RULES_VERSION,
      generatedAt: now.toISOString(),
      deviceFingerprint: deviceInfo?.fingerprint || 'unknown',
    },
    device: deviceInfo
      ? {
          manufacturer: deviceInfo.manufacturer,
          model: deviceInfo.model,
          brand: deviceInfo.brand,
          androidVersion: deviceInfo.androidVersion,
          sdkVersion: deviceInfo.sdkVersion,
          hardware: deviceInfo.hardware,
          board: deviceInfo.board,
          totalRam: deviceInfo.totalRam,
          totalStorage: deviceInfo.totalStorage,
          processorCount: deviceInfo.processorCount,
          buildDisplay: deviceInfo.buildDisplay,
          securityPatch: deviceInfo.securityPatch,
          fingerprint: deviceInfo.fingerprint,
          bootloader: deviceInfo.bootloader,
          buildTags: deviceInfo.buildTags,
          buildType: deviceInfo.buildType,
        }
      : null,
    risk: risk
      ? {
          score: risk.score,
          level: risk.level,
          recommendation: risk.recommendation,
          triggeredRules: risk.triggeredRules.map(r => ({
            ruleId: r.ruleId,
            name: r.name,
            category: r.category,
            severity: r.severity,
            evidence: r.evidence,
            source: r.source,
          })),
        }
      : null,
    packages: {
      total: packages.packages.length,
      byCategory: packageStats,
      items: packages.packages.slice(0, 100).map(p => ({
        packageName: p.packageName,
        name: p.label,
        classification: p.classification?.category || 'UNKNOWN',
        isSystemApp: p.flags.system,
        indicators: p.indicators,
      })),
    },
    adminIndicators: control.indicators.map(i => ({
      type: i.type,
      packageName: i.packageName,
      status: i.status,
      confidence: i.confidence,
    })),
    integrity: integrity
      ? {
          overallStatus: integrity.overallStatus,
          indicators: integrity.indicators.map(i => ({
            type: i.type,
            status: i.status,
            confidence: i.confidence,
            source: i.source,
            value: i.value,
          })),
        }
      : null,
  };
}

export function reportToJson(report: DiagnosticReport): string {
  return JSON.stringify(report, null, 2);
}

export function reportToSummary(report: DiagnosticReport): string {
  const lines: string[] = [];

  lines.push('=== INFORME DE DIAGNÓSTICO ===');
  lines.push(`Fecha: ${report.metadata.generatedAt}`);
  lines.push(`App: v${report.metadata.appVersion}`);
  lines.push('');

  if (report.device) {
    lines.push('--- DISPOSITIVO ---');
    lines.push(`${report.device.manufacturer} ${report.device.model}`);
    lines.push(`Android ${report.device.androidVersion} (SDK ${report.device.sdkVersion})`);
    lines.push(`Parche: ${report.device.securityPatch}`);
    lines.push('');
  }

  if (report.risk) {
    lines.push('--- RIESGO ---');
    lines.push(`Nivel: ${report.risk.level} (${report.risk.score}/100)`);
    lines.push(report.risk.recommendation);
    lines.push('');
  }

  lines.push('--- APLICACIONES ---');
  lines.push(`Total: ${report.packages.total}`);
  for (const [cat, count] of Object.entries(report.packages.byCategory)) {
    lines.push(`  ${cat}: ${count}`);
  }
  lines.push('');

  lines.push('--- ADMINISTRACIÓN ---');
  lines.push(`Indicadores: ${report.adminIndicators.length}`);
  for (const ind of report.adminIndicators) {
    lines.push(`  ${ind.type}: ${ind.packageName} (${ind.status})`);
  }
  lines.push('');

  if (report.integrity) {
    lines.push('--- INTEGRIDAD ---');
    lines.push(`Estado: ${report.integrity.overallStatus}`);
    lines.push(`Indicadores: ${report.integrity.indicators.length}`);
  }

  return lines.join('\n');
}
