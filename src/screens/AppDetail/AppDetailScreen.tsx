import React, {useMemo, useState} from 'react';
import {View, Text, ScrollView, TouchableOpacity, StyleSheet, SafeAreaView} from 'react-native';
import {useRoute, useNavigation} from '@react-navigation/native';
import type {RouteProp} from '@react-navigation/native';
import type {RootStackParamList} from '../../types/navigation';
import {useAnalysis} from '../../context/AnalysisContext';
import {calculateAllApps, getLevelLabel, getLevelColor} from '../../domain/apps';
import type {AppScore, AppFactor} from '../../domain/apps';
import {colors} from '../../theme/colors';

type AppDetailRouteProp = RouteProp<RootStackParamList, 'AppDetail'>;

const FACTOR_EXPLANATIONS: Record<string, string> = {
  DEVICE_OWNER:
    'Un-aplicación es Device Owner, lo que le da control total sobre el dispositivo. Puede configurar políticas de seguridad, borrar datos, bloquear funciones y administrar otras aplicaciones.',
  PROFILE_OWNER:
    'Una aplicación es Profile Owner dentro de un perfil de trabajo. Puede restringir apps, configuración y datos dentro de ese perfil.',
  DEVICE_ADMIN:
    'Una aplicación tiene privilegios de Device Administrator. Puede bloquear el dispositivo, cambiar contraseña, borrar datos y restringir funciones específicas.',
  ACCESSIBILITY_SERVICE:
    'Un servicio de accesibilidad de terceros está habilitado. Puede leer el contenido de la pantalla, capturar gestos, y controlar el dispositivo en nombre del usuario.',
  VPN_SERVICE:
    'Un servicio VPN está activo. Puede interceptar y redirigir todo el tráfico de red del dispositivo.',
  OVERLAY:
    'La aplicación tiene permiso de overlay (dibujar sobre otras apps). Puede mostrar ventanas encima de otras aplicaciones, potencialmente para phishing.',
  FORCE_LOCK:
    'La aplicación puede bloquear el dispositivo forzosamente sin intervención del usuario.',
  WIPE_CAPABILITY:
    'La aplicación tiene la capacidad de borrar todos los datos del dispositivo (factory reset).',
  BOOT_RECEIVER:
    'La aplicación se ejecuta automáticamente al encender el dispositivo. Esto permite que procesos se inicien sin intervención del usuario.',
  PERSISTENT_SERVICE:
    'La aplicación ejecuta un servicio persistente que no se puede detener fácilmente. Se mantiene activo en segundo plano permanentemente.',
  ADMINISTRATIVE_POLICY:
    'La aplicación aplica políticas administrativas que pueden restringir funciones del dispositivo.',
  ROOT_BINARY:
    'Se detectó un binario de root (su, suid) en el sistema. El dispositivo tiene acceso de superusuario.',
  ROOT_APP:
    'Se detectó una aplicación de root conocida (Magisk, SuperSU, etc.). Indica que el dispositivo fue modificado para obtener acceso root.',
  'Root Indicator':
    'Se detectaron indicios de acceso root en el dispositivo.',
  'Mod Framework':
    'Se detectó un framework de modificación (Xposed, LSPosed, etc.). Permite modificar el comportamiento del sistema y de otras aplicaciones.',
  DEVICE_POLICY_CONTROLLER:
    'La aplicación actúa como controlador de políticas de dispositivo. Puede configurar y administrar restricciones del sistema.',
};

const getExplanation = (label: string): string => {
  return FACTOR_EXPLANATIONS[label] || 'Capacidad detectada en la aplicación.';
};

const FactorItem = ({factor, levelColor}: {factor: AppFactor; levelColor: string}) => {
  const [expanded, setExpanded] = useState(false);

  return (
    <TouchableOpacity
      style={styles.factorContainer}
      onPress={() => setExpanded(!expanded)}
      activeOpacity={0.7}>
      <View style={styles.factorRow}>
        <View style={styles.factorCheck}>
          <Text style={styles.checkIcon}>✓</Text>
        </View>
        <Text style={styles.factorLabel}>{factor.label}</Text>
        <Text style={styles.factorPoints}>+{factor.points}</Text>
        <View style={[styles.factorStatus, {backgroundColor: levelColor}]}>
          <Text style={styles.factorStatusText}>{factor.status}</Text>
        </View>
      </View>
      {expanded && (
        <View style={styles.explanationContainer}>
          <Text style={styles.explanationText}>{getExplanation(factor.label)}</Text>
        </View>
      )}
    </TouchableOpacity>
  );
};

export const AppDetailScreen = () => {
  const route = useRoute<AppDetailRouteProp>();
  const navigation = useNavigation();
  const {packageName} = route.params;
  const {result, controlResult, integrityResult} = useAnalysis();
  const [permissionsExpanded, setPermissionsExpanded] = useState(false);

  const appScore = useMemo((): AppScore | null => {
    if (!result || !controlResult || !integrityResult) {
      return null;
    }
    const scores = calculateAllApps(result, controlResult, integrityResult);
    return scores.find(s => s.packageName === packageName) || null;
  }, [result, controlResult, integrityResult, packageName]);

  const pkg = useMemo(() => {
    if (!result) {
      return null;
    }
    return result.packages.find(p => p.packageName === packageName) || null;
  }, [result, packageName]);

  if (!appScore) {
    return (
      <SafeAreaView style={styles.container}>
        <View style={styles.empty}>
          <Text style={styles.emptyText}>Cargando...</Text>
        </View>
      </SafeAreaView>
    );
  }

  const levelColor = getLevelColor(appScore.level);

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.content}>
        {/* Header */}
        <View style={styles.header}>
          <Text style={styles.appName}>{appScore.name}</Text>
          <Text style={styles.packageName}>{appScore.packageName}</Text>
        </View>

        {/* Score */}
        <View style={styles.scoreSection}>
          <Text style={[styles.score, {color: levelColor}]}>{appScore.score}/100</Text>
          <View style={[styles.levelBadge, {backgroundColor: levelColor}]}>
            <Text style={styles.levelText}>{getLevelLabel(appScore.level)}</Text>
          </View>
        </View>

        {/* Factors */}
        {appScore.factors.length > 0 && (
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>CAPACIDADES DETECTADAS</Text>
            <Text style={styles.sectionHint}>Toca para ver más detalles</Text>
            {appScore.factors.map((factor, idx) => (
              <FactorItem key={idx} factor={factor} levelColor={levelColor} />
            ))}
          </View>
        )}

        {/* Package Info */}
        {pkg && (
          <View style={styles.section}>
            <Text style={styles.sectionTitle}>INFORMACIÓN</Text>
            <InfoRow label="Package" value={pkg.packageName} />
            <InfoRow label="Versión" value={pkg.version?.name || '—'} />
            <InfoRow label="Instalador" value={pkg.installerPackageName || '—'} />
            <InfoRow label="Categoría" value={appScore.classification} />
            <InfoRow label="Sistema" value={pkg.flags.system ? 'Sí' : 'No'} />
          </View>
        )}

        {/* Declared Permissions */}
        {pkg && pkg.declaredPermissions.length > 0 && (
          <TouchableOpacity
            style={styles.section}
            onPress={() => setPermissionsExpanded(!permissionsExpanded)}
            activeOpacity={0.8}>
            <View style={styles.sectionHeader}>
              <Text style={styles.sectionTitle}>
                PERMISOS DECLARADOS ({pkg.declaredPermissions.length})
              </Text>
              <Text style={styles.expandIcon}>{permissionsExpanded ? '▼' : '▶'}</Text>
            </View>
            {(permissionsExpanded
              ? pkg.declaredPermissions
              : pkg.declaredPermissions.slice(0, 20)
            ).map((perm, idx) => (
              <Text key={idx} style={styles.permText}>{perm}</Text>
            ))}
            {!permissionsExpanded && pkg.declaredPermissions.length > 20 && (
              <Text style={styles.moreText}>
                +{pkg.declaredPermissions.length - 20} más (toca para ver todos)
              </Text>
            )}
          </TouchableOpacity>
        )}
      </ScrollView>
    </SafeAreaView>
  );
};

const InfoRow = ({label, value}: {label: string; value: string}) => (
  <View style={infoStyles.row}>
    <Text style={infoStyles.label}>{label}</Text>
    <Text style={infoStyles.value} numberOfLines={1}>{value}</Text>
  </View>
);

const infoStyles = StyleSheet.create({
  row: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    paddingVertical: 8,
    borderBottomWidth: 1,
    borderBottomColor: colors.divider,
  },
  label: {
    fontSize: 13,
    color: colors.textSecondary,
    flex: 1,
  },
  value: {
    fontSize: 13,
    color: colors.text,
    fontWeight: '500',
    flex: 1.5,
    textAlign: 'right',
  },
});

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
  content: {
    padding: 16,
  },
  empty: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  emptyText: {
    fontSize: 14,
    color: colors.textMuted,
  },
  header: {
    marginBottom: 16,
  },
  appName: {
    fontSize: 22,
    fontWeight: '800',
    color: colors.text,
  },
  packageName: {
    fontSize: 13,
    color: colors.textMuted,
    marginTop: 2,
  },
  scoreSection: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: colors.surface,
    borderRadius: 12,
    padding: 20,
    marginBottom: 16,
    gap: 12,
  },
  score: {
    fontSize: 36,
    fontWeight: '800',
  },
  levelBadge: {
    paddingHorizontal: 10,
    paddingVertical: 5,
    borderRadius: 6,
  },
  levelText: {
    fontSize: 12,
    fontWeight: '700',
    color: '#FFFFFF',
    letterSpacing: 0.5,
  },
  section: {
    backgroundColor: colors.surface,
    borderRadius: 12,
    padding: 16,
    marginBottom: 12,
  },
  sectionTitle: {
    fontSize: 13,
    fontWeight: '700',
    color: colors.text,
    letterSpacing: 1,
    marginBottom: 4,
  },
  sectionHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 4,
  },
  expandIcon: {
    fontSize: 10,
    color: colors.textMuted,
  },
  sectionHint: {
    fontSize: 11,
    color: colors.textMuted,
    marginBottom: 10,
  },
  factorContainer: {
    borderBottomWidth: 1,
    borderBottomColor: colors.divider,
  },
  factorRow: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 10,
    gap: 8,
  },
  factorCheck: {
    width: 20,
    height: 20,
    borderRadius: 10,
    backgroundColor: '#27AE60',
    alignItems: 'center',
    justifyContent: 'center',
  },
  checkIcon: {
    fontSize: 12,
    color: '#FFFFFF',
    fontWeight: '700',
  },
  factorLabel: {
    fontSize: 14,
    fontWeight: '600',
    color: colors.text,
    flex: 1,
  },
  factorPoints: {
    fontSize: 14,
    fontWeight: '700',
    color: colors.text,
  },
  factorStatus: {
    paddingHorizontal: 6,
    paddingVertical: 2,
    borderRadius: 4,
  },
  factorStatusText: {
    fontSize: 9,
    fontWeight: '600',
    color: '#FFFFFF',
  },
  explanationContainer: {
    backgroundColor: colors.card,
    borderRadius: 8,
    padding: 12,
    marginBottom: 8,
  },
  explanationText: {
    fontSize: 12,
    color: colors.textSecondary,
    lineHeight: 18,
  },
  permText: {
    fontSize: 12,
    color: colors.textSecondary,
    paddingVertical: 4,
    borderBottomWidth: 1,
    borderBottomColor: colors.divider,
  },
  moreText: {
    fontSize: 12,
    color: '#3498DB',
    marginTop: 8,
  },
});
