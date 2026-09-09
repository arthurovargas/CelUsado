import React from 'react';
import {createNativeStackNavigator} from '@react-navigation/native-stack';
import {MainScreen} from '../screens/Main/MainScreen';
import {AppDetailScreen} from '../screens/AppDetail/AppDetailScreen';
import type {RootStackParamList} from '../types/navigation';

const Stack = createNativeStackNavigator<RootStackParamList>();

export const AppNavigator = () => {
  return (
    <Stack.Navigator
      initialRouteName="Main"
      screenOptions={{
        headerStyle: {backgroundColor: '#FFFFFF'},
        headerTintColor: '#1A1A2E',
        headerTitleStyle: {fontWeight: '600'},
        contentStyle: {backgroundColor: '#F5F5F5'},
      }}>
      <Stack.Screen name="Main" component={MainScreen} options={{title: 'CelUsado'}} />
      <Stack.Screen
        name="AppDetail"
        component={AppDetailScreen}
        options={{title: 'Detalle de aplicación'}}
      />
    </Stack.Navigator>
  );
};
