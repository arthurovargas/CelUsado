export type AppClassification = 'SYSTEM' | 'OEM' | 'GOOGLE' | 'CARRIER' | 'USER' | 'UNKNOWN';

export type AppIndicator = {
  type: string;
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  evidence: string;
};

export type ApplicationInfo = {
  packageName: string;
  name: string;
  versionName: string;
  versionCode: number;
  firstInstallTime: number;
  lastUpdateTime: number;
  isSystemApp: boolean;
  isUpdatedSystemApp: boolean;
  installerPackageName: string | null;
  classification: AppClassification;
  classificationConfidence: number;
  declaredPermissions: string[];
  services: ServiceInfo[];
  receivers: ReceiverInfo[];
  activities: ActivityInfo[];
  providers: ProviderInfo[];
  signingInfo: SigningInfoData;
  indicators: AppIndicator[];
  isDeviceAdmin: boolean;
  isDeviceOwner: boolean;
  isProfileOwner: boolean;
  hasAccessibilityService: boolean;
  hasOverlay: boolean;
};

export type ServiceInfo = {
  name: string;
  permission: string | null;
  foregroundServiceType: string | null;
  isExported: boolean;
};

export type ReceiverInfo = {
  name: string;
  permission: string | null;
  isExported: boolean;
};

export type ActivityInfo = {
  name: string;
  permission: string | null;
  isExported: boolean;
};

export type ProviderInfo = {
  name: string;
  authority: string | null;
  isExported: boolean;
  readPermission: string | null;
  writePermission: string | null;
};

export type SigningInfoData = {
  schemeVersion: number;
  hasMultipleSigners: boolean;
  hasPastSigningCertificates: boolean;
  currentSigners: string[];
  historicalSigners: string[];
};
