export type RiskLevel = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export type RuleCategory =
  | 'ROOT_DETECTION'
  | 'ADMIN_CONTROL'
  | 'INTEGRITY_COMPROMISE'
  | 'KNOWN_MDM'
  | 'SUSPICIOUS_PERMISSIONS'
  | 'MODIFICATION_FRAMEWORK'
  | 'CUSTOM_ROM'
  | 'BUILD_ANOMALY'
  | 'SECURITY_PATCH_STALE';

export type RuleSeverity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';

export interface RuleDefinition {
  id: string;
  name: string;
  description: string;
  category: RuleCategory;
  severity: RuleSeverity;
  weight: number;
  enabled: boolean;
}

export interface TriggeredRule {
  ruleId: string;
  name: string;
  category: RuleCategory;
  severity: RuleSeverity;
  weight: number;
  evidence: string;
  source: 'PACKAGE' | 'CONTROL' | 'INTEGRITY' | 'COMBINED';
  matchingApps: {
    packageName: string;
    name: string;
    reason: string;
  }[];
}

export interface RiskResult {
  score: number;
  level: RiskLevel;
  triggeredRules: TriggeredRule[];
  summary: {
    totalRulesTriggered: number;
    bySeverity: {
      critical: number;
      high: number;
      medium: number;
      low: number;
    };
    byCategory: Record<RuleCategory, number>;
  };
  recommendation: string;
  timestamp: number;
}
