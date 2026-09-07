import React from 'react';
import {createNativeStackNavigator} from '@react-navigation/native-stack';
import {HomeScreen} from '../screens/Home/HomeScreen';
import {ScanScreen} from '../screens/Scan/ScanScreen';
import {ApplicationsScreen} from '../screens/Applications/ApplicationsScreen';
import {ApplicationDetailScreen} from '../screens/ApplicationDetail/ApplicationDetailScreen';
import {DeviceScreen} from '../screens/Device/DeviceScreen';
import {ReportScreen} from '../screens/Report/ReportScreen';
import type {RootStackParamList} from '../types/navigation';

const Stack = createNativeStackNavigator<RootStackParamList>();

export const AppNavigator = () => {
  return (
    <Stack.Navigator
      initialRouteName="Home"
      screenOptions={{
        headerStyle: {backgroundColor: '#FFFFFF'},
        headerTintColor: '#1A1A2E',
        headerTitleStyle: {fontWeight: '600'},
        contentStyle: {backgroundColor: '#F5F5F5'},
      }}>
      <Stack.Screen name="Home" component={HomeScreen} options={{title: 'CelUsado'}} />
      <Stack.Screen name="Scan" component={ScanScreen} options={{title: 'Analizar equipo'}} />
      <Stack.Screen
        name="Applications"
        component={ApplicationsScreen}
        options={{title: 'Aplicaciones'}}
      />
      <Stack.Screen
        name="ApplicationDetail"
        component={ApplicationDetailScreen}
        options={{title: 'Detalle de aplicación'}}
      />
      <Stack.Screen
        name="Device"
        component={DeviceScreen}
        options={{title: 'Información del dispositivo'}}
      />
      <Stack.Screen name="Report" component={ReportScreen} options={{title: 'Informe'}} />
    </Stack.Navigator>
  );
};
