import React from 'react';
import {View, Text, ScrollView, StyleSheet, SafeAreaView} from 'react-native';
import {useRoute, type RouteProp} from '@react-navigation/native';
import type {RootStackParamList} from '../../types/navigation';
import {useAnalysis} from '../../context/AnalysisContext';

type RouteProps = RouteProp<RootStackParamList, 'ApplicationDetail'>;

export const ApplicationDetailScreen = () => {
  const route = useRoute<RouteProps>();
  const {packageName} = route.params;
  const {result} = useAnalysis();

  const pkg = result?.packages.find(p => p.packageName === packageName);

  if (!pkg) {
    return (
      <SafeAreaView style={styles.container}>
        <ScrollView contentContainerStyle={styles.content}>
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>Paquete</Text>
            <Text style={styles.value}>{packageName}</Text>
          </View>
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>Información</Text>
            <Text style={styles.placeholder}>
              No se encontraron datos para esta aplicación.
            </Text>
          </View>
        </ScrollView>
      </SafeAreaView>
    );
  }

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.content}>
        {/* Identity */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Identidad</Text>
          <DetailRow label="Nombre" value={pkg.label} />
          <DetailRow label="Paquete" value={pkg.packageName} />
          <DetailRow label="Versión" value={`${pkg.version.name} (${pkg.version.code})`} />
          <DetailRow
            label="Instalado"
            value={new Date(pkg.firstInstallTime).toLocaleDateString()}
          />
          <DetailRow
            label="Actualizado"
            value={new Date(pkg.lastUpdateTime).toLocaleDateString()}
          />
          {pkg.installerPackageName && (
            <DetailRow label="Instalador" value={pkg.installerPackageName} />
          )}
        </View>

        {/* Flags */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Estado</Text>
          <DetailRow label="Sistema" value={pkg.flags.system ? 'Sí' : 'No'} />
          <DetailRow label="Actualizado" value={pkg.flags.updatedSystem ? 'Sí' : 'No'} />
          <DetailRow label="Debug" value={pkg.flags.debuggable ? 'Sí' : 'No'} />
        </View>

        {/* Classification */}
        {pkg.classification && (
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>Clasificación</Text>
            <DetailRow label="Categoría" value={pkg.classification.category} />
            <DetailRow
              label="Confianza"
              value={`${(pkg.classification.confidence * 100).toFixed(1)}%`}
            />
            <DetailRow label="Evidencia" value={pkg.classification.evidence.join(', ')} />
          </View>
        )}

        {/* Discovery Sources */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Fuentes de descubrimiento</Text>
          <Text style={styles.value}>{pkg.discoverySources.join(', ')}</Text>
        </View>

        {/* Permissions */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>
            Permisos ({pkg.declaredPermissions.length})
          </Text>
          {pkg.declaredPermissions.slice(0, 20).map((perm, idx) => (
            <Text key={idx} style={styles.listItem}>
              {perm}
            </Text>
          ))}
          {pkg.declaredPermissions.length > 20 && (
            <Text style={styles.moreText}>
              +{pkg.declaredPermissions.length - 20} más
            </Text>
          )}
        </View>

        {/* Services */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Servicios ({pkg.services.length})</Text>
          {pkg.services.slice(0, 10).map((svc, idx) => (
            <Text key={idx} style={styles.listItem}>
              {svc.name}
            </Text>
          ))}
          {pkg.services.length > 10 && (
            <Text style={styles.moreText}>+{pkg.services.length - 10} más</Text>
          )}
        </View>

        {/* Receivers */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>
            Receptores ({pkg.receivers.length})
          </Text>
          {pkg.receivers.slice(0, 10).map((rcv, idx) => (
            <Text key={idx} style={styles.listItem}>
              {rcv.name}
            </Text>
          ))}
          {pkg.receivers.length > 10 && (
            <Text style={styles.moreText}>+{pkg.receivers.length - 10} más</Text>
          )}
        </View>

        {/* Indicators */}
        {pkg.indicators.length > 0 && (
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>
              Indicadores ({pkg.indicators.length})
            </Text>
            {pkg.indicators.map((ind, idx) => (
              <Text key={idx} style={styles.listItem}>
                {ind}
              </Text>
            ))}
          </View>
        )}

        {/* Signing */}
        {pkg.signing && (
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>Firma</Text>
            <DetailRow
              label="Firmantes múltiples"
              value={pkg.signing.hasMultipleSigners ? 'Sí' : 'No'}
            />
            <DetailRow
              label="Histórico"
              value={pkg.signing.hasPastSigningCertificates ? 'Sí' : 'No'}
            />
            {pkg.signing.currentSigners.map((signer, idx) => (
              <DetailRow key={idx} label="Firmante" value={signer.substring(0, 20) + '...'} />
            ))}
          </View>
        )}
      </ScrollView>
    </SafeAreaView>
  );
};

const DetailRow = ({label, value}: {label: string; value: string}) => (
  <View style={detailStyles.row}>
    <Text style={detailStyles.label}>{label}</Text>
    <Text style={detailStyles.value}>{value}</Text>
  </View>
);

const detailStyles = StyleSheet.create({
  row: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    paddingVertical: 6,
    borderBottomWidth: 1,
    borderBottomColor: '#F5F5F5',
  },
  label: {
    fontSize: 13,
    color: '#666',
    flex: 1,
  },
  value: {
    fontSize: 13,
    color: '#1A1A2E',
    fontWeight: '500',
    flex: 1.5,
    textAlign: 'right',
  },
});

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F5F5F5',
  },
  content: {
    padding: 16,
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
  value: {
    fontSize: 14,
    color: '#1A1A2E',
    fontWeight: '500',
  },
  listItem: {
    fontSize: 12,
    color: '#666',
    paddingVertical: 4,
    borderBottomWidth: 1,
    borderBottomColor: '#F5F5F5',
  },
  moreText: {
    fontSize: 12,
    color: '#3498DB',
    fontWeight: '500',
    marginTop: 8,
  },
  placeholder: {
    fontSize: 14,
    color: '#999',
    fontStyle: 'italic',
  },
});
