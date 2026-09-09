import type {PackageAnalysisResult} from '../../modules/android/PackageAnalyzer';
import type {DeviceControlResult} from '../../modules/android/DeviceControl';
import type {IntegrityResult} from '../../modules/android/NativeIntegrity';
import type {
  RuleDefinition,
  TriggeredRule,
  RiskResult,
  RiskLevel,
  RuleCategory,
} from './RiskTypes';
import riskRules from '../../../src/rules/risk-rules.json';

const RULES: RuleDefinition[] = riskRules as RuleDefinition[];

function evaluateRootDetection(
  packages: PackageAnalysisResult,
): TriggeredRule[] {
  const triggered: TriggeredRule[] = [];
  const rootIndicators = packages.packages.filter(
    p =>
      p.indicators?.some(
        ind =>
          ind === 'ROOT_BINARY' ||
          ind === 'ROOT_APP' ||
          ind === 'SU_BINARY' ||
          ind === 'SUID_BINARY' ||
          ind.includes('root') ||
          ind.includes('su_binary'),
      ) ||
      p.classification?.category === 'USER' &&
        (p.packageName.includes('magisk') ||
          p.packageName.includes('supersu') ||
          p.packageName.includes('superuser') ||
          p.packageName.includes('kingroot') ||
          p.packageName.includes('kingo')),
  );

  const byRule = new Map<string, {rule: RuleDefinition; apps: {packageName: string; name: string; reason: string}[]}>();

  for (const pkg of rootIndicators) {
    const hasBinary = pkg.indicators?.some(
      ind => ind === 'ROOT_BINARY' || ind === 'SU_BINARY' || ind === 'SUID_BINARY',
    );
    const ruleId = hasBinary ? 'ROOT_BINARY_DETECTED' : 'ROOT_APP_DETECTED';
    const rule = RULES.find(r => r.id === ruleId);
    if (!rule || !rule.enabled) continue;

    if (!byRule.has(ruleId)) {
      byRule.set(ruleId, {rule, apps: []});
    }
    byRule.get(ruleId)!.apps.push({
      packageName: pkg.packageName,
      name: pkg.label,
      reason: hasBinary ? 'Binario de root detectado' : 'Aplicación de root conocida',
    });
  }

  for (const [ruleId, {rule, apps}] of byRule) {
    triggered.push({
      ruleId,
      name: rule.name,
      category: rule.category,
      severity: rule.severity,
      weight: rule.weight,
      evidence: apps.map(a => a.packageName).join(', '),
      source: 'PACKAGE',
      matchingApps: apps,
    });
  }

  return triggered;
}

function evaluateAdminControl(
  control: DeviceControlResult,
): TriggeredRule[] {
  const triggered: TriggeredRule[] = [];
  const byRule = new Map<string, {rule: RuleDefinition; apps: {packageName: string; name: string; reason: string}[]}>();

  for (const indicator of control.indicators) {
    if (
      indicator.status !== 'CONFIRMED' &&
      indicator.status !== 'ACTIVE' &&
      indicator.status !== 'DECLARED'
    ) {
      continue;
    }

    let ruleId: string | null = null;

    switch (indicator.type) {
      case 'DEVICE_OWNER':
        ruleId = 'DEVICE_OWNER_ACTIVE';
        break;
      case 'PROFILE_OWNER':
        ruleId = 'PROFILE_OWNER_ACTIVE';
        break;
      case 'DEVICE_ADMIN':
        ruleId = 'DEVICE_ADMIN_ACTIVE';
        break;
      case 'ACCESSIBILITY_SERVICE':
        ruleId = 'ACCESSIBILITY_SERVICE_ACTIVE';
        break;
      case 'VPN_SERVICE':
        ruleId = 'VPN_SERVICE_ACTIVE';
        break;
      case 'OVERLAY':
        ruleId = 'OVERLAY_DETECTED';
        break;
    }

    if (!ruleId) continue;
    const rule = RULES.find(r => r.id === ruleId);
    if (!rule || !rule.enabled) continue;

    if (!byRule.has(ruleId)) {
      byRule.set(ruleId, {rule, apps: []});
    }

    const statusLabel = indicator.status === 'CONFIRMED' ? 'Confirmado' :
      indicator.status === 'ACTIVE' ? 'Activo' : 'Declarado';

    byRule.get(ruleId)!.apps.push({
      packageName: indicator.packageName,
      name: indicator.packageName,
      reason: `${indicator.type} - ${statusLabel} (${(indicator.confidence * 100).toFixed(0)}% confianza)`,
    });
  }

  for (const [ruleId, {rule, apps}] of byRule) {
    triggered.push({
      ruleId,
      name: rule.name,
      category: rule.category,
      severity: rule.severity,
      weight: rule.weight,
      evidence: apps.map(a => a.packageName).join(', '),
      source: 'CONTROL',
      matchingApps: apps,
    });
  }

  return triggered;
}

function evaluateIntegrity(
  integrity: IntegrityResult,
): TriggeredRule[] {
  const triggered: TriggeredRule[] = [];
  const byRule = new Map<string, {rule: RuleDefinition; apps: {packageName: string; name: string; reason: string}[]}>();

  for (const indicator of integrity.indicators) {
    if (indicator.status !== 'CONFIRMED' && indicator.status !== 'ACTIVE') {
      continue;
    }

    let ruleId: string | null = null;

    switch (indicator.type) {
      case 'BUILD_TEST_KEYS':
        ruleId = 'TEST_KEYS_BUILD';
        break;
      case 'CUSTOM_ROM':
        ruleId = 'CUSTOM_ROM_DETECTED';
        break;
      case 'ROOT_BINARY':
      case 'ROOT_APP':
        ruleId = 'ROOT_APP_DETECTED';
        break;
      case 'MODIFICATION_FRAMEWORK':
        ruleId = 'MODIFICATION_FRAMEWORK';
        break;
      case 'BOOTLOADER_UNLOCKED':
        ruleId = 'BOOTLOADER_UNLOCKED';
        break;
      case 'VERIFIED_BOOT_FAIL':
        ruleId = 'VERIFIED_BOOT_FAIL';
        break;
    }

    if (!ruleId) continue;
    const rule = RULES.find(r => r.id === ruleId);
    if (!rule || !rule.enabled) continue;

    if (!byRule.has(ruleId)) {
      byRule.set(ruleId, {rule, apps: []});
    }

    const value = indicator.value || `Estado: ${indicator.status}`;
    byRule.get(ruleId)!.apps.push({
      packageName: 'system',
      name: 'Sistema',
      reason: `${indicator.type} - ${value} (${(indicator.confidence * 100).toFixed(0)}% confianza)`,
    });
  }

  for (const [ruleId, {rule, apps}] of byRule) {
    triggered.push({
      ruleId,
      name: rule.name,
      category: rule.category,
      severity: rule.severity,
      weight: rule.weight,
      evidence: apps.map(a => a.reason).join('; '),
      source: 'INTEGRITY',
      matchingApps: apps,
    });
  }

  return triggered;
}

function evaluateSecurityPatch(
  packages: PackageAnalysisResult,
): TriggeredRule[] {
  const triggered: TriggeredRule[] = [];
  const rule = RULES.find(r => r.id === 'SECURITY_PATCH_STALE');
  if (!rule || !rule.enabled) return triggered;

  const patchIndicators = packages.packages[0]?.indicators?.filter(
    ind => ind === 'SECURITY_PATCH' || ind.includes('security_patch'),
  );

  if (patchIndicators && patchIndicators.length > 0) {
    const patchEvidence = patchIndicators[0];
    if (patchEvidence && patchEvidence.includes('days_old:')) {
      const daysMatch = patchEvidence.match(/days_old:\s*(\d+)/);
      if (daysMatch && daysMatch[1]) {
        const daysOld = parseInt(daysMatch[1], 10);
        if (daysOld > 90) {
          triggered.push({
            ruleId: 'SECURITY_PATCH_STALE',
            name: rule.name,
            category: rule.category,
            severity: rule.severity,
            weight: rule.weight,
            evidence: `Parche de seguridad tiene ${daysOld} días`,
            source: 'INTEGRITY',
            matchingApps: [{
              packageName: 'system',
              name: 'Sistema',
              reason: `Parche de seguridad obsoleto (${daysOld} días)`,
            }],
          });
        }
      }
    }
  }

  return triggered;
}

function calculateRiskLevel(score: number): RiskLevel {
  if (score >= 60) return 'CRITICAL';
  if (score >= 35) return 'HIGH';
  if (score >= 15) return 'MEDIUM';
  return 'LOW';
}

function getRecommendation(level: RiskLevel, triggeredCount: number): string {
  switch (level) {
    case 'CRITICAL':
      return `Riesgo crítico detectado (${triggeredCount} indicadores). Este dispositivo tiene evidencia fuerte de modificaciones que comprometen su integridad. Se recomienda NO adquirirlo sin verificación presencial de un técnico especializado.`;
    case 'HIGH':
      return `Riesgo alto detectado (${triggeredCount} indicadores). El dispositivo tiene señales de administración o modificación. Se recomienda verificar el estado real del dispositivo antes de la compra.`;
    case 'MEDIUM':
      return `Riesgo moderado detectado (${triggeredCount} indicadores). Se recomienda revisar los indicadores mostrados y confirmar que son legítimos (ej: administrador corporativo justificado).`;
    case 'LOW':
      return `Riesgo bajo. No se detectaron indicadores significativos de administración o modificación. El dispositivo parece estar en estado estándar.`;
  }
}

export function evaluateRisk(
  packages: PackageAnalysisResult,
  control: DeviceControlResult,
  integrity: IntegrityResult,
): RiskResult {
  const allTriggered: TriggeredRule[] = [
    ...evaluateRootDetection(packages),
    ...evaluateAdminControl(control),
    ...evaluateIntegrity(integrity),
    ...evaluateSecurityPatch(packages),
  ];

  const merged = new Map<string, TriggeredRule>();
  for (const rule of allTriggered) {
    const existing = merged.get(rule.ruleId);
    if (existing) {
      existing.matchingApps = [...existing.matchingApps, ...rule.matchingApps];
      existing.evidence = existing.evidence + ', ' + rule.evidence;
    } else {
      merged.set(rule.ruleId, {...rule});
    }
  }

  const deduplicated = Array.from(merged.values());

  const totalWeight = deduplicated.reduce((sum, r) => sum + r.weight, 0);
  const score = Math.min(totalWeight, 100);
  const level = calculateRiskLevel(score);

  const bySeverity = {
    critical: deduplicated.filter(r => r.severity === 'CRITICAL').length,
    high: deduplicated.filter(r => r.severity === 'HIGH').length,
    medium: deduplicated.filter(r => r.severity === 'MEDIUM').length,
    low: deduplicated.filter(r => r.severity === 'LOW').length,
  };

  const byCategory: Record<RuleCategory, number> = {} as Record<RuleCategory, number>;
  for (const rule of deduplicated) {
    byCategory[rule.category] = (byCategory[rule.category] || 0) + 1;
  }

  return {
    score,
    level,
    triggeredRules: deduplicated,
    summary: {
      totalRulesTriggered: deduplicated.length,
      bySeverity,
      byCategory,
    },
    recommendation: getRecommendation(level, deduplicated.length),
    timestamp: Date.now(),
  };
}
