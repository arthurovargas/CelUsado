export type RootStackParamList = {
  Home: undefined;
  Scan: undefined;
  Applications: undefined;
  ApplicationDetail: {packageName: string};
  Device: undefined;
  Report: undefined;
};

declare global {
  namespace ReactNavigation {
    interface RootParamList extends RootStackParamList {}
  }
}
