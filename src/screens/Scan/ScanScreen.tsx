import React, {useState, useCallback} from 'react';
import {View, Text, TouchableOpacity, StyleSheet, SafeAreaView} from 'react-native';
import {useNavigation} from '@react-navigation/native';
import type {NativeStackNavigationProp} from '@react-navigation/native-stack';
import type {RootStackParamList} from '../../types/navigation';
import {fetchDeviceInfo} from '../../domain/device';
import {logger} from '../../utils';

type NavigationProp = NativeStackNavigationProp<RootStackParamList, 'Scan'>;

type ScanStep = {
  id: string;
  label: string;
  status: 'pending' | 'running' | 'done' | 'error';
};

const INITIAL_STEPS: ScanStep[] = [
  {id: 'device', label: 'Información del equipo', status: 'pending'},
  {id: 'policy', label: 'Política del dispositivo', status: 'pending'},
  {id: 'admin', label: 'Administradores', status: 'pending'},
  {id: 'accessibility', label: 'Accesibilidad', status: 'pending'},
  {id: 'vpn', label: 'VPN', status: 'pending'},
  {id: 'integrity', label: 'Integridad', status: 'pending'},
  {id: 'apps', label: 'Aplicaciones', status: 'pending'},
  {id: 'report', label: 'Generar informe', status: 'pending'},
];

const TAG = 'ScanScreen';

export const ScanScreen = () => {
  const navigation = useNavigation<NavigationProp>();
  const [steps, setSteps] = useState<ScanStep[]>(INITIAL_STEPS);
  const [isScanning, setIsScanning] = useState(false);

  const updateStep = useCallback((index: number, status: ScanStep['status']) => {
    setSteps(prev => prev.map((step, idx) => (idx === index ? {...step, status} : step)));
  }, []);

  const runScan = async () => {
    setIsScanning(true);
    setSteps(INITIAL_STEPS);

    for (let i = 0; i < INITIAL_STEPS.length; i++) {
      updateStep(i, 'running');

      try {
        switch (INITIAL_STEPS[i]!.id) {
          case 'device':
            await fetchDeviceInfo();
            break;
          default:
            await new Promise(resolve => setTimeout(resolve, 500));
            break;
        }
        updateStep(i, 'done');
      } catch (error) {
        logger.error(TAG, `Step ${INITIAL_STEPS[i]!.id} failed`, error);
        updateStep(i, 'error');
      }
    }

    setIsScanning(false);
    navigation.navigate('Report');
  };

  const getStatusIcon = (status: ScanStep['status']) => {
    switch (status) {
      case 'pending':
        return '○';
      case 'running':
        return '◎';
      case 'done':
        return '✓';
      case 'error':
        return '✗';
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.content}>
        <View style={styles.stepsContainer}>
          {steps.map(step => (
            <View key={step.id} style={styles.stepRow}>
              <Text
                style={[
                  styles.stepIcon,
                  step.status === 'done' && styles.stepIconDone,
                  step.status === 'running' && styles.stepIconRunning,
                  step.status === 'error' && styles.stepIconError,
                ]}>
                {getStatusIcon(step.status)}
              </Text>
              <Text
                style={[
                  styles.stepLabel,
                  step.status === 'done' && styles.stepLabelDone,
                  step.status === 'running' && styles.stepLabelRunning,
                ]}>
                {step.label}
              </Text>
            </View>
          ))}
        </View>

        <TouchableOpacity
          style={[styles.scanButton, isScanning && styles.scanButtonDisabled]}
          onPress={runScan}
          disabled={isScanning}>
          <Text style={styles.scanButtonText}>
            {isScanning ? 'Analizando...' : 'Iniciar análisis'}
          </Text>
        </TouchableOpacity>
      </View>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F5F5F5',
  },
  content: {
    flex: 1,
    padding: 24,
    justifyContent: 'space-between',
  },
  stepsContainer: {
    backgroundColor: '#FFFFFF',
    borderRadius: 12,
    padding: 20,
    shadowColor: '#000',
    shadowOffset: {width: 0, height: 2},
    shadowOpacity: 0.08,
    shadowRadius: 8,
    elevation: 3,
  },
  stepRow: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 12,
    borderBottomWidth: 1,
    borderBottomColor: '#F0F0F0',
  },
  stepIcon: {
    width: 28,
    fontSize: 18,
    color: '#CCC',
    textAlign: 'center',
  },
  stepIconDone: {
    color: '#2ECC71',
  },
  stepIconRunning: {
    color: '#3498DB',
  },
  stepIconError: {
    color: '#E74C3C',
  },
  stepLabel: {
    fontSize: 15,
    color: '#999',
    flex: 1,
  },
  stepLabelDone: {
    color: '#1A1A2E',
  },
  stepLabelRunning: {
    color: '#3498DB',
    fontWeight: '500',
  },
  scanButton: {
    backgroundColor: '#1A1A2E',
    borderRadius: 12,
    padding: 18,
    alignItems: 'center',
  },
  scanButtonDisabled: {
    backgroundColor: '#CCC',
  },
  scanButtonText: {
    color: '#FFFFFF',
    fontSize: 16,
    fontWeight: '700',
  },
});
