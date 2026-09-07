import React from 'react';
import {View, Text, ScrollView, StyleSheet, SafeAreaView} from 'react-native';
import {useAnalysis} from '../../context/AnalysisContext';
import type {DeviceControlIndicator} from '../../modules/android/DeviceControl';

const getIndicatorTypeLabel = (type: string) => {
  switch (type) {
    case 'DEVICE_ADMIN':
      return 'Device Administrator';
    case 'DEVICE_OWNER':
      return 'Device Owner';
    case 'PROFILE_OWNER':
      return 'Profile Owner';
    case 'DEVICE_POLICY_CONTROLLER':
      return 'Device Policy Controller';
    case 'ACCESSIBILITY_SERVICE':
      return 'Accessibility Service';
    case 'VPN_SERVICE':
      return 'VPN Service';
    case 'OVERLAY':
      return 'Overlay';
    case 'BOOT_RECEIVER':
      return 'Boot Receiver';
    case 'PERSISTENT_SERVICE':
      return 'Persistent Service';
    case 'FORCE_LOCK':
      return 'Force Lock';
    case 'WIPE_CAPABILITY':
      return 'Wipe/Reset';
    default:
      return type;
  }
};

const getStatusColor = (status: string) => {
  switch (status) {
    case 'CONFIRMED':
    case 'ACTIVE':
      return '#E74C3C';
    case 'DECLARED':
    case 'CAPABLE':
      return '#F39C12';
    case 'DETECTED':
      return '#3498DB';
    case 'NOT_DETECTED':
    case 'NOT_ACCESSIBLE':
    case 'NOT_AVAILABLE':
    case 'NOT_DETERMINABLE':
      return '#95A5A6';
    default:
      return '#BDC3C7';
  }
};

const getStatusLabel = (status: string) => {
  switch (status) {
    case 'CONFIRMED':
      return 'Confirmado';
    case 'ACTIVE':
      return 'Activo';
    case 'DECLARED':
      return 'Declarado';
    case 'CAPABLE':
      return 'Capaz';
    case 'DETECTED':
      return 'Detectado';
    case 'NOT_DETECTED':
      return 'No detectado';
    case 'NOT_ACCESSIBLE':
      return 'No accesible';
    case 'NOT_AVAILABLE':
      return 'No disponible';
    case 'NOT_DETERMINABLE':
      return 'No determinable';
    default:
      return status;
  }
};

export const ReportScreen = () => {
  const {result, controlResult} = useAnalysis();

  const packages = result?.packages || [];
  const coverage = result?.coverage;
  const controlIndicators = controlResult?.indicators || [];
  const controlCoverage = controlResult?.coverage;

  // Classification stats
  const stats = {
    total: packages.length,
    system: packages.filter(p => p.classification?.category === 'SYSTEM').length,
    oem: packages.filter(p => p.classification?.category === 'OEM').length,
    google: packages.filter(p => p.classification?.category === 'GOOGLE').length,
    carrier: packages.filter(p => p.classification?.category === 'CARRIER').length,
    user: packages.filter(p => p.classification?.category === 'USER').length,
    unknown: packages.filter(p => !p.classification || p.classification.category === 'UNKNOWN').length,
  };

  // High-priority control indicators (CONFIRMED, ACTIVE, DECLARED)
  const highPriorityIndicators = controlIndicators.filter(
    ind => ind.status === 'CONFIRMED' || ind.status === 'ACTIVE' || ind.status === 'DECLARED',
  );

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.content}>
        <View style={styles.header}>
          <Text style={styles.title}>INFORME DE DIAGNÓSTICO</Text>
        </View>

        {/* Phase 3 Coverage */}
        {coverage && (
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>COBERTURA - ANÁLISIS DE APLICACIONES</Text>
            <View style={styles.coverageRow}>
              <View style={styles.coverageItem}>
                <Text style={styles.coverageValue}>{coverage.packagesDiscovered}</Text>
                <Text style={styles.coverageLabel}>Descubiertos</Text>
              </View>
              <View style={styles.coverageItem}>
                <Text style={styles.coverageValue}>{coverage.packagesAnalyzed}</Text>
                <Text style={styles.coverageLabel}>Analizados</Text>
              </View>
              <View style={styles.coverageItem}>
                <Text style={[styles.coverageValue, styles.coverageStatus]}>
                  {coverage.status}
                </Text>
                <Text style={styles.coverageLabel}>Estado</Text>
              </View>
            </View>
          </View>
        )}

        {/* Phase 4 Coverage */}
        {controlCoverage && (
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>COBERTURA - ANÁLISIS DE ADMINISTRACIÓN</Text>
            <View style={styles.coverageRow}>
              <View style={styles.coverageItem}>
                <Text style={styles.coverageValue}>{controlIndicators.length}</Text>
                <Text style={styles.coverageLabel}>Indicadores</Text>
              </View>
              <View style={styles.coverageItem}>
                <Text style={[styles.coverageValue, styles.coverageStatus]}>
                  {controlCoverage.status}
                </Text>
                <Text style={styles.coverageLabel}>Estado</Text>
              </View>
            </View>
            {controlCoverage.limitations.length > 0 && (
              <Text style={styles.limitationText}>
                Limitaciones: {controlCoverage.limitations.slice(0, 3).join(', ')}
                {controlCoverage.limitations.length > 3 && ` +${controlCoverage.limitations.length - 3} más`}
              </Text>
            )}
          </View>
        )}

        {/* Classification Summary */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>CLASIFICACIÓN DE APLICACIONES</Text>
          <View style={styles.statsGrid}>
            <StatItem label="Sistema" value={stats.system} color="#3498DB" />
            <StatItem label="OEM" value={stats.oem} color="#9B59B6" />
            <StatItem label="Google" value={stats.google} color="#2ECC71" />
            <StatItem label="Operador" value={stats.carrier} color="#F39C12" />
            <StatItem label="Usuario" value={stats.user} color="#95A5A6" />
            <StatItem label="Desconocido" value={stats.unknown} color="#BDC3C7" />
          </View>
        </View>

        {/* Device Control Indicators - Phase 4 */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>
            INDICADORES DE ADMINISTRACIÓN ({controlIndicators.length})
          </Text>
          {controlIndicators.length === 0 ? (
            <Text style={styles.placeholder}>No se detectaron indicadores de administración.</Text>
          ) : (
            controlIndicators.slice(0, 15).map((indicator, idx) => (
              <IndicatorRow key={idx} indicator={indicator} />
            ))
          )}
          {controlIndicators.length > 15 && (
            <Text style={styles.moreText}>+{controlIndicators.length - 15} más</Text>
          )}
        </View>

        {/* All Packages Summary */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>
            TODAS LAS APLICACIONES ({packages.length})
          </Text>
          {packages.slice(0, 20).map((pkg, idx) => (
            <View key={idx} style={styles.packageRow}>
              <Text style={styles.packageName}>{pkg.label}</Text>
              <Text
                style={[
                  styles.packageClass,
                  {color: getClassificationColor(pkg.classification?.category || null)},
                ]}>
                {pkg.classification?.category || 'UNKNOWN'}
              </Text>
            </View>
          ))}
          {packages.length > 20 && (
            <Text style={styles.moreText}>+{packages.length - 20} más</Text>
          )}
        </View>

        {/* Conclusion */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>CONCLUSIÓN</Text>
          {highPriorityIndicators.length > 0 ? (
            <Text style={styles.conclusionWarning}>
              Se detectaron {highPriorityIndicators.length} indicador(es) de administración o
              control del dispositivo con estado confirmado, activo o declarado.
            </Text>
          ) : (
            <Text style={styles.conclusionOk}>
              No se detectaron indicadores confirmados de administración o control.
            </Text>
          )}
          <Text style={styles.disclaimer}>
            Este informe constituye evidencia técnica, no una conclusión definitiva. La
            interpretación de riesgo corresponde a una fase posterior.
          </Text>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
};

const IndicatorRow = ({indicator}: {indicator: DeviceControlIndicator}) => (
  <View style={indicatorStyles.row}>
    <View style={indicatorStyles.info}>
      <Text style={indicatorStyles.type}>{getIndicatorTypeLabel(indicator.type)}</Text>
      <Text style={indicatorStyles.package}>{indicator.packageName}</Text>
    </View>
    <View style={indicatorStyles.right}>
      <View style={[indicatorStyles.statusBadge, {backgroundColor: getStatusColor(indicator.status)}]}>
        <Text style={indicatorStyles.statusText}>{getStatusLabel(indicator.status)}</Text>
      </View>
      <Text style={indicatorStyles.confidence}>
        {(indicator.confidence * 100).toFixed(0)}%
      </Text>
    </View>
  </View>
);

const indicatorStyles = StyleSheet.create({
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 10,
    borderBottomWidth: 1,
    borderBottomColor: '#F5F5F5',
  },
  info: {
    flex: 1,
  },
  type: {
    fontSize: 14,
    fontWeight: '600',
    color: '#1A1A2E',
  },
  package: {
    fontSize: 11,
    color: '#999',
    marginTop: 2,
  },
  right: {
    alignItems: 'flex-end',
    gap: 4,
  },
  statusBadge: {
    paddingHorizontal: 8,
    paddingVertical: 3,
    borderRadius: 4,
  },
  statusText: {
    fontSize: 10,
    fontWeight: '600',
    color: '#FFFFFF',
  },
  confidence: {
    fontSize: 11,
    color: '#666',
  },
});

const StatItem = ({label, value, color}: {label: string; value: number; color: string}) => (
  <View style={statStyles.item}>
    <Text style={[statStyles.value, {color}]}>{value}</Text>
    <Text style={statStyles.label}>{label}</Text>
  </View>
);

const statStyles = StyleSheet.create({
  item: {
    alignItems: 'center',
    flex: 1,
  },
  value: {
    fontSize: 20,
    fontWeight: '700',
  },
  label: {
    fontSize: 11,
    color: '#999',
    marginTop: 4,
  },
});

const getClassificationColor = (category: string | null) => {
  switch (category) {
    case 'SYSTEM':
      return '#3498DB';
    case 'OEM':
      return '#9B59B6';
    case 'GOOGLE':
      return '#2ECC71';
    case 'CARRIER':
      return '#F39C12';
    case 'USER':
      return '#95A5A6';
    default:
      return '#BDC3C7';
  }
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F5F5F5',
  },
  content: {
    padding: 16,
  },
  header: {
    marginBottom: 24,
    alignItems: 'center',
  },
  title: {
    fontSize: 18,
    fontWeight: '800',
    color: '#1A1A2E',
    letterSpacing: 2,
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
    marginBottom: 12,
    fontWeight: '600',
  },
  placeholder: {
    fontSize: 14,
    color: '#999',
    fontStyle: 'italic',
  },
  coverageRow: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    marginBottom: 12,
  },
  coverageItem: {
    alignItems: 'center',
  },
  coverageValue: {
    fontSize: 24,
    fontWeight: '700',
    color: '#1A1A2E',
  },
  coverageStatus: {
    fontSize: 14,
    color: '#F39C12',
  },
  coverageLabel: {
    fontSize: 11,
    color: '#999',
    marginTop: 4,
  },
  limitationText: {
    fontSize: 12,
    color: '#E74C3C',
    textAlign: 'center',
  },
  statsGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
  },
  packageRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    paddingVertical: 8,
    borderBottomWidth: 1,
    borderBottomColor: '#F5F5F5',
  },
  packageName: {
    fontSize: 13,
    color: '#1A1A2E',
    flex: 1,
  },
  packageClass: {
    fontSize: 12,
    fontWeight: '600',
  },
  moreText: {
    fontSize: 12,
    color: '#3498DB',
    fontWeight: '500',
    marginTop: 8,
  },
  conclusionWarning: {
    fontSize: 14,
    color: '#E67E22',
    fontWeight: '500',
    marginBottom: 12,
  },
  conclusionOk: {
    fontSize: 14,
    color: '#27AE60',
    fontWeight: '500',
    marginBottom: 12,
  },
  disclaimer: {
    fontSize: 12,
    color: '#999',
    fontStyle: 'italic',
    lineHeight: 18,
  },
});
