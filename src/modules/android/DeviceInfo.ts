import NativeDeviceInfo from './NativeDeviceInfo';
import {logger} from '../../utils';

export interface RawDeviceInfo {
  manufacturer: string;
  model: string;
  brand: string;
  device: string;
  product: string;
  hardware: string;
  board: string;
  androidVersion: string;
  sdkVersion: number;
  buildNumber: string;
  securityPatch: string;
  fingerprint: string;
  bootloader: string;
  buildTags: string;
  buildType: string;
  buildDisplay: string;
  buildTime: number;
  totalRam: number;
  availableRam: number;
  totalStorage: number;
  availableStorage: number;
  processorCount: number;
  maxMemory: number;
  batteryLevel: number;
  batteryStatus: number;
  batteryHealth: number;
  batteryTemperature: number;
  isCharging: boolean;
}

const TAG = 'DeviceInfo';

export async function getRawDeviceInfo(): Promise<RawDeviceInfo> {
  try {
    logger.info(TAG, 'Fetching device info from native module');
    const raw = await NativeDeviceInfo.getDeviceInfo();
    logger.info(TAG, 'Device info retrieved successfully', {
      manufacturer: raw.manufacturer,
      model: raw.model,
    });
    return raw as unknown as RawDeviceInfo;
  } catch (error) {
    logger.error(TAG, 'Failed to get device info', error);
    throw error;
  }
}
