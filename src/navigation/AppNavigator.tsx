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
        headerStyle: {backgroundColor: '#16213E'},
        headerTintColor: '#FFFFFF',
        headerTitleStyle: {fontWeight: '600', color: '#FFFFFF'},
        contentStyle: {backgroundColor: '#1A1A2E'},
      }}>
      <Stack.Screen
        name="Main"
        component={MainScreen}
        options={{headerShown: false}}
      />
      <Stack.Screen
        name="AppDetail"
        component={AppDetailScreen}
        options={{title: 'Detalle de aplicación'}}
      />
    </Stack.Navigator>
  );
};
