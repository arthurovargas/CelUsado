import {NativeModules, Platform} from 'react-native';

export interface NativeDeviceControlModule {
  analyzeDeviceControl(): Promise<Record<string, unknown>>;
}

const LINKING_ERROR =
  `The package 'DeviceControl' doesn't seem to be linked. Make sure:\n\n` +
  Platform.select({ios: "- You have run 'pod install'\n", default: ''}) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const DeviceControlModule: NativeDeviceControlModule =
  NativeModules.AppDeviceControl
    ? NativeModules.AppDeviceControl
    : new Proxy({} as NativeDeviceControlModule, {
        get() {
          throw new Error(LINKING_ERROR);
        },
      });

export default DeviceControlModule;
