export type DeviceAdminInfo = {
  packageName: string;
  componentName: string;
  label: string;
  isActive: boolean;
};

export type DevicePolicyInfo = {
  hasDeviceOwner: boolean;
  deviceOwnerPackage: string | null;
  deviceOwnerLabel: string | null;
  hasProfileOwner: boolean;
  profileOwnerPackage: string | null;
  profileOwnerLabel: string | null;
  isOrganizationOwned: boolean;
};

export type AccessibilityServiceInfo = {
  id: string;
  packageName: string;
  serviceName: string;
  label: string;
  feedbackType: number;
  capabilities: number;
  eventTypes: number;
  packageNames: string[] | null;
  isEnabled: boolean;
};

export type VpnStatus = {
  isActiveOnCurrentNetwork: boolean;
  isActiveOnAnyNetwork: boolean;
  interfaceNames: string[];
};

export type IntegrityIndicators = {
  buildTags: string;
  buildType: string;
  isTestKeys: boolean;
  isDebuggableBuild: boolean;
  fingerprint: string;
  bootloader: string;
};

export type ScanResult = {
  deviceInfo: import('./device').DeviceInfo;
  devicePolicy: DevicePolicyInfo;
  deviceAdmins: DeviceAdminInfo[];
  accessibilityServices: AccessibilityServiceInfo[];
  vpnStatus: VpnStatus;
  integrity: IntegrityIndicators;
  applications: import('./application').ApplicationInfo[];
  timestamp: number;
};
