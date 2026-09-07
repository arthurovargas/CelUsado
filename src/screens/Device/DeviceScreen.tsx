import React from 'react';
import {View, Text, ScrollView, StyleSheet, SafeAreaView, ActivityIndicator} from 'react-native';
import {useDeviceInfo} from '../../hooks/useDeviceInfo';
import {formatRam, formatStorage, getBuildTagsLabel, getBuildTypeLabel} from '../../domain/device';

export const DeviceScreen = () => {
  const {data, isLoading, error} = useDeviceInfo();

  if (isLoading) {
    return (
      <SafeAreaView style={styles.container}>
        <View style={styles.centered}>
          <ActivityIndicator size="large" color="#1A1A2E" />
          <Text style={styles.loadingText}>Obteniendo información del dispositivo...</Text>
        </View>
      </SafeAreaView>
    );
  }

  if (error) {
    return (
      <SafeAreaView style={styles.container}>
        <View style={styles.centered}>
          <Text style={styles.errorText}>Error al obtener información</Text>
          <Text style={styles.errorDetail}>{error}</Text>
        </View>
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.content}>
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Identificación</Text>
          <InfoRow label="Fabricante" value={data?.manufacturer ?? '—'} />
          <InfoRow label="Modelo" value={data?.model ?? '—'} />
          <InfoRow label="Marca" value={data?.brand ?? '—'} />
          <InfoRow label="Android" value={data?.androidVersion ?? '—'} />
          <InfoRow label="SDK" value={data?.sdkVersion?.toString() ?? '—'} />
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Hardware</Text>
          <InfoRow label="SoC" value={data?.hardware ?? '—'} />
          <InfoRow label="RAM" value={data?.totalRam ? formatRam(data.totalRam) : '—'} />
          <InfoRow
            label="Almacenamiento"
            value={data?.totalStorage ? formatStorage(data.totalStorage) : '—'}
          />
          <InfoRow
            label="CPU"
            value={data?.processorCount ? `${data.processorCount} núcleos` : '—'}
          />
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Build</Text>
          <InfoRow label="Versión" value={data?.buildDisplay ?? '—'} />
          <InfoRow label="Parche seguridad" value={data?.securityPatch ?? '—'} />
          <InfoRow label="Fingerprint" value={data?.fingerprint ?? '—'} />
          <InfoRow label="Bootloader" value={data?.bootloader ?? '—'} />
          <InfoRow
            label="Build Tags"
            value={data?.buildTags ? getBuildTagsLabel(data.buildTags) : '—'}
          />
          <InfoRow
            label="Build Type"
            value={data?.buildType ? getBuildTypeLabel(data.buildType) : '—'}
          />
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Batería</Text>
          <InfoRow
            label="Nivel"
            value={
              data?.batteryLevel != null && data.batteryLevel >= 0 ? `${data.batteryLevel}%` : '—'
            }
          />
          <InfoRow label="Estado" value={data?.batteryStatus ?? '—'} />
          <InfoRow label="Salud" value={data?.batteryHealth ?? '—'} />
          <InfoRow
            label="Temperatura"
            value={
              data?.batteryTemperature != null && data.batteryTemperature >= 0
                ? `${data.batteryTemperature.toFixed(1)}°C`
                : '—'
            }
          />
          <InfoRow label="Cargando" value={data?.isCharging ? 'Sí' : 'No'} />
        </View>
      </ScrollView>
    </SafeAreaView>
  );
};

const InfoRow = ({label, value}: {label: string; value: string}) => (
  <View style={infoStyles.row}>
    <Text style={infoStyles.label}>{label}</Text>
    <Text style={infoStyles.value} numberOfLines={2}>
      {value}
    </Text>
  </View>
);

const infoStyles = StyleSheet.create({
  row: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    paddingVertical: 10,
    borderBottomWidth: 1,
    borderBottomColor: '#F0F0F0',
  },
  label: {
    fontSize: 14,
    color: '#666',
  },
  value: {
    fontSize: 14,
    color: '#1A1A2E',
    fontWeight: '500',
    flex: 1,
    textAlign: 'right',
  },
});

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F5F5F5',
  },
  content: {
    padding: 24,
  },
  centered: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 24,
  },
  loadingText: {
    marginTop: 16,
    fontSize: 16,
    color: '#666',
  },
  errorText: {
    fontSize: 18,
    fontWeight: '600',
    color: '#E74C3C',
    marginBottom: 8,
  },
  errorDetail: {
    fontSize: 14,
    color: '#666',
    textAlign: 'center',
  },
  section: {
    backgroundColor: '#FFFFFF',
    borderRadius: 12,
    padding: 16,
    marginBottom: 16,
    shadowColor: '#000',
    shadowOffset: {width: 0, height: 1},
    shadowOpacity: 0.05,
    shadowRadius: 4,
    elevation: 1,
  },
  sectionTitle: {
    fontSize: 12,
    color: '#999',
    textTransform: 'uppercase',
    letterSpacing: 1,
    marginBottom: 8,
  },
});
