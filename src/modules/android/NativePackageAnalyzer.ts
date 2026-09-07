import {NativeModules, Platform} from 'react-native';

export interface NativePackageAnalyzerModule {
  analyzeAllPackages(): Promise<Record<string, unknown>>;
}

const LINKING_ERROR =
  `The package 'PackageAnalyzer' doesn't seem to be linked. Make sure:\n\n` +
  Platform.select({ios: "- You have run 'pod install'\n", default: ''}) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const PackageAnalyzerModule: NativePackageAnalyzerModule =
  NativeModules.AppPackageAnalyzer
    ? NativeModules.AppPackageAnalyzer
    : new Proxy({} as NativePackageAnalyzerModule, {
        get() {
          throw new Error(LINKING_ERROR);
        },
      });

export default PackageAnalyzerModule;
