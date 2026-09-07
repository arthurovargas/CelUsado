import {NativeModules, Platform} from 'react-native';

// Log para depuración: veremos esto en la terminal de Metro
console.log('Available NativeModules:', Object.keys(NativeModules));

export interface NativeDeviceInfoModule {
  getDeviceInfo(): Promise<Record<string, unknown>>;
}

const LINKING_ERROR =
  `The package 'DeviceInfo' doesn't seem to be linked. Make sure:\n\n` +
  Platform.select({ios: "- You have run 'pod install'\n", default: ''}) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const DeviceInfoModule: NativeDeviceInfoModule = NativeModules.AppDeviceInfo
  ? NativeModules.AppDeviceInfo
  : new Proxy({} as NativeDeviceInfoModule, {
      get() {
        throw new Error(LINKING_ERROR);
      },
    });

export default DeviceInfoModule;
