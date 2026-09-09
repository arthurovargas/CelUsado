export type RootStackParamList = {
  Main: undefined;
  AppDetail: {packageName: string};
};

declare global {
  namespace ReactNavigation {
    interface RootParamList extends RootStackParamList {}
  }
}
