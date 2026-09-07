import React from 'react';
import {StatusBar} from 'react-native';
import {NavigationContainer} from '@react-navigation/native';
import {SafeAreaProvider} from 'react-native-safe-area-context';
import {AppNavigator} from './src/navigation/AppNavigator';
import {ErrorBoundary} from './src/utils/ErrorBoundary';
import {AnalysisProvider} from './src/context/AnalysisContext';

const App = () => {
  return (
    <ErrorBoundary>
      <SafeAreaProvider>
        <AnalysisProvider>
          <NavigationContainer>
            <StatusBar barStyle="dark-content" backgroundColor="#FFFFFF" />
            <AppNavigator />
          </NavigationContainer>
        </AnalysisProvider>
      </SafeAreaProvider>
    </ErrorBoundary>
  );
};

export default App;
