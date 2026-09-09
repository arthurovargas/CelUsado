import React from 'react';
import {View, Text, StyleSheet} from 'react-native';
import type {DeviceInfoExtended} from '../../domain/device/DeviceInfoService';
import {colors} from '../../theme/colors';

interface DeviceHeaderProps {
  deviceInfo: DeviceInfoExtended | null;
}

export const DeviceHeader = ({deviceInfo}: DeviceHeaderProps) => {
  if (!deviceInfo) {
    return (
      <View style={styles.container}>
        <Text style={styles.title}>CelUsado</Text>
        <Text style={styles.manufacturer}>Cargando información...</Text>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <Text style={styles.title}>
        CelUsado{' '}
        <Text style={styles.brand}>{deviceInfo.manufacturer}</Text>
      </Text>
      <Text style={styles.model}>{deviceInfo.model}</Text>
      <View style={styles.details}>
        <Text style={styles.detail}>Android {deviceInfo.androidVersion}</Text>
        <Text style={styles.separator}>·</Text>
        <Text style={styles.detail}>Parche: {deviceInfo.securityPatch || '—'}</Text>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    backgroundColor: colors.surface,
    borderRadius: 12,
    padding: 16,
    marginBottom: 12,
  },
  title: {
    fontSize: 20,
    fontWeight: '800',
    color: colors.text,
    letterSpacing: 0.5,
  },
  brand: {
    fontSize: 20,
    fontWeight: '400',
    color: colors.textSecondary,
  },
  manufacturer: {
    fontSize: 14,
    color: colors.textMuted,
    fontWeight: '500',
  },
  model: {
    fontSize: 22,
    fontWeight: '800',
    color: '#3498DB',
    marginTop: 2,
  },
  details: {
    flexDirection: 'row',
    alignItems: 'center',
    marginTop: 6,
    gap: 6,
  },
  detail: {
    fontSize: 12,
    color: colors.textSecondary,
  },
  separator: {
    fontSize: 12,
    color: colors.textMuted,
  },
});
