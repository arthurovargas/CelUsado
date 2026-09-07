import NativeDeviceControl from './NativeDeviceControl';
import {logger} from '../../utils';

export type DetectionStatus =
  | 'CONFIRMED'
  | 'ACTIVE'
  | 'DECLARED'
  | 'CAPABLE'
  | 'DETECTED'
  | 'NOT_DETECTED'
  | 'NOT_ACCESSIBLE'
  | 'NOT_AVAILABLE'
  | 'NOT_DETERMINABLE'
  | 'UNKNOWN';

export type IndicatorType =
  | 'DEVICE_ADMIN'
  | 'DEVICE_OWNER'
  | 'PROFILE_OWNER'
  | 'DEVICE_POLICY_CONTROLLER'
  | 'ACCESSIBILITY_SERVICE'
  | 'VPN_SERVICE'
  | 'OVERLAY'
  | 'BOOT_RECEIVER'
  | 'PERSISTENT_SERVICE'
  | 'FORCE_LOCK'
  | 'WIPE_CAPABILITY'
  | 'ADMINISTRATIVE_POLICY';

export type EvidenceSource =
  | 'MANIFEST'
  | 'PACKAGE_COMPONENT'
  | 'SYSTEM_API'
  | 'INDIRECT_DETECTION'
  | 'KNOWN_PACKAGE'
  | 'PERMISSION'
  | 'INTENT_FILTER'
  | 'SYSTEM_STATE'
  | 'CONFIGURATION';

export interface EvidenceItem {
  type: string;
  source: EvidenceSource;
  value: string | null;
  description: string | null;
}

export interface DeviceControlIndicator {
  type: IndicatorType;
  packageName: string;
  status: DetectionStatus;
  confidence: number;
  evidence: EvidenceItem[];
  limitations: string[];
}

export interface ControlCoverage {
  status: string;
  analyzersExecuted: string[];
  limitations: string[];
}

export interface DeviceControlResult {
  indicators: DeviceControlIndicator[];
  coverage: ControlCoverage;
}

const TAG = 'DeviceControl';

export async function analyzeDeviceControl(): Promise<DeviceControlResult> {
  try {
    logger.info(TAG, 'Starting device control analysis from native module');
    const raw = await NativeDeviceControl.analyzeDeviceControl();
    logger.info(TAG, 'Device control analysis completed successfully');
    return raw as unknown as DeviceControlResult;
  } catch (error) {
    logger.error(TAG, 'Failed to analyze device control', error);
    throw error;
  }
}
