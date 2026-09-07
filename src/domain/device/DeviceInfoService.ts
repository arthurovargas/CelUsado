import {getRawDeviceInfo} from '../../modules/android';
import type {RawDeviceInfo} from '../../modules/android';
import type {DeviceInfo} from '../../types';
import {logger} from '../../utils';

const TAG = 'DeviceInfoService';

export type BatteryStatus = 'UNKNOWN' | 'CHARGING' | 'DISCHARGING' | 'NOT_CHARGING' | 'FULL';

export type BatteryHealth =
  | 'UNKNOWN'
  | 'GOOD'
  | 'OVERHEAT'
  | 'DEAD'
  | 'OVER_VOLTAGE'
  | 'UNSPECIFIED_FAILURE'
  | 'COLD';

export interface DeviceInfoExtended extends DeviceInfo {
  batteryLevel: number;
  batteryStatus: BatteryStatus;
  batteryHealth: BatteryHealth;
  batteryTemperature: number;
  isCharging: boolean;
}

function mapBatteryStatus(status: number): BatteryStatus {
  switch (status) {
    case 2:
      return 'CHARGING';
    case 3:
      return 'DISCHARGING';
    case 4:
      return 'NOT_CHARGING';
    case 5:
      return 'FULL';
    default:
      return 'UNKNOWN';
  }
}

function mapBatteryHealth(health: number): BatteryHealth {
  switch (health) {
    case 2:
      return 'GOOD';
    case 3:
      return 'OVERHEAT';
    case 4:
      return 'DEAD';
    case 5:
      return 'OVER_VOLTAGE';
    case 6:
      return 'UNSPECIFIED_FAILURE';
    case 7:
      return 'COLD';
    default:
      return 'UNKNOWN';
  }
}

function mapRawToDeviceInfo(raw: RawDeviceInfo): DeviceInfoExtended {
  return {
    manufacturer: raw.manufacturer,
    model: raw.model,
    brand: raw.brand,
    device: raw.device,
    product: raw.product,
    hardware: raw.hardware,
    board: raw.board,
    androidVersion: raw.androidVersion,
    sdkVersion: raw.sdkVersion,
    buildNumber: raw.buildNumber,
    securityPatch: raw.securityPatch,
    fingerprint: raw.fingerprint,
    bootloader: raw.bootloader,
    buildTags: raw.buildTags,
    buildType: raw.buildType,
    buildDisplay: raw.buildDisplay,
    buildTime: raw.buildTime,
    totalRam: raw.totalRam,
    availableRam: raw.availableRam,
    totalStorage: raw.totalStorage,
    availableStorage: raw.availableStorage,
    processorCount: raw.processorCount,
    maxMemory: raw.maxMemory,
    batteryLevel: raw.batteryLevel,
    batteryStatus: mapBatteryStatus(raw.batteryStatus),
    batteryHealth: mapBatteryHealth(raw.batteryHealth),
    batteryTemperature: raw.batteryTemperature,
    isCharging: raw.isCharging,
  };
}

export async function fetchDeviceInfo(): Promise<DeviceInfoExtended> {
  logger.info(TAG, 'Fetching device info');
  const raw = await getRawDeviceInfo();
  const info = mapRawToDeviceInfo(raw);
  logger.info(TAG, 'Device info mapped', {
    manufacturer: info.manufacturer,
    model: info.model,
    androidVersion: info.androidVersion,
  });
  return info;
}

export function formatBytes(bytes: number): string {
  if (bytes === 0) {
    return '0 B';
  }
  const units = ['B', 'KB', 'MB', 'GB', 'TB'];
  const i = Math.floor(Math.log(bytes) / Math.log(1024));
  const value = bytes / Math.pow(1024, i);
  return `${value.toFixed(1)} ${units[i] ?? ''}`;
}

export function formatRam(bytes: number): string {
  const gb = bytes / (1024 * 1024 * 1024);
  if (gb >= 1) {
    return `${gb.toFixed(1)} GB`;
  }
  const mb = bytes / (1024 * 1024);
  return `${mb.toFixed(0)} MB`;
}

export function formatStorage(bytes: number): string {
  return formatBytes(bytes);
}

export function getDisplayManufacturer(info: DeviceInfoExtended): string {
  return info.manufacturer || 'Desconocido';
}

export function getDisplayModel(info: DeviceInfoExtended): string {
  return info.model || 'Desconocido';
}

export function getDisplayAndroidVersion(info: DeviceInfoExtended): string {
  return info.androidVersion || 'Desconocido';
}

export function getBuildTagsLabel(tags: string): string {
  switch (tags) {
    case 'release-keys':
      return 'Release Keys';
    case 'test-keys':
      return 'Test Keys';
    default:
      return tags;
  }
}

export function getBuildTypeLabel(type: string): string {
  switch (type) {
    case 'user':
      return 'User';
    case 'userdebug':
      return 'User Debug';
    case 'eng':
      return 'Engineering';
    default:
      return type;
  }
}
