import React from 'react';
import renderer from 'react-test-renderer';
import App from '../App';

jest.mock('@react-navigation/native', () => {
  const actual = jest.requireActual('@react-navigation/native');
  return {
    ...actual,
    NavigationContainer: ({children}: {children: React.ReactNode}) => children,
  };
});

jest.mock('@react-navigation/native-stack', () => ({
  createNativeStackNavigator: () => {
    const Screen = () => null;
    const Navigator = ({children}: {children: React.ReactNode}) => children;
    return {Navigator, Screen};
  },
}));

jest.mock('react-native-safe-area-context', () => ({
  SafeAreaProvider: ({children}: {children: React.ReactNode}) => children,
  useSafeAreaInsets: () => ({top: 0, bottom: 0, left: 0, right: 0}),
  useSafeAreaFrame: () => ({x: 0, y: 0, width: 0, height: 0}),
}));

jest.mock('react-native-gesture-handler', () => ({
  GestureHandlerRootView: ({children}: {children: React.ReactNode}) => children,
}));

jest.mock('react-native-reanimated', () => ({
  default: {
    createAnimatedComponent: (component: React.ComponentType) => component,
  },
  useSharedValue: () => 0,
  useAnimatedStyle: () => ({}),
  withTiming: (value: number) => value,
  withSpring: (value: number) => value,
}));

jest.mock('react-native-screens', () => ({
  Screen: ({children}: {children: React.ReactNode}) => children,
  ScreenContainer: ({children}: {children: React.ReactNode}) => children,
}));

describe('App', () => {
  it('is a valid React component', () => {
    expect(App).toBeDefined();
    expect(typeof App).toBe('function');
  });
});
