import React from 'react';
import {View, Text, FlatList, TouchableOpacity, StyleSheet, SafeAreaView} from 'react-native';
import {useNavigation} from '@react-navigation/native';
import type {NativeStackNavigationProp} from '@react-navigation/native-stack';
import type {RootStackParamList} from '../../types/navigation';
import {useAnalysis} from '../../context/AnalysisContext';
import type {NormalizedPackage} from '../../modules/android/PackageAnalyzer';

type NavigationProp = NativeStackNavigationProp<RootStackParamList, 'Applications'>;

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

export const ApplicationsScreen = () => {
  const navigation = useNavigation<NavigationProp>();
  const {result} = useAnalysis();

  const packages = result?.packages || [];

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.headerTitle}>Aplicaciones detectadas</Text>
        <Text style={styles.headerSubtitle}>
          {packages.length} aplicaciones encontradas
          {result?.coverage ? ` · Cobertura: ${result.coverage.status}` : ''}
        </Text>
      </View>

      <FlatList
        data={packages}
        keyExtractor={item => item.packageName}
        contentContainerStyle={styles.list}
        renderItem={({item}) => (
          <TouchableOpacity
            style={styles.appCard}
            onPress={() =>
              navigation.navigate('ApplicationDetail', {
                packageName: item.packageName,
              })
            }>
            <View style={styles.appInfo}>
              <Text style={styles.appName}>{item.label}</Text>
              <Text style={styles.appPackage}>{item.packageName}</Text>
              <View style={styles.tagsRow}>
                {item.flags.system && <Text style={styles.tag}>SYS</Text>}
                {item.flags.updatedSystem && <Text style={styles.tag}>UPD</Text>}
                {item.discoverySources.length > 0 && (
                  <Text style={styles.tag}>{item.discoverySources[0]}</Text>
                )}
              </View>
            </View>
            <View
              style={[
                styles.badge,
                {backgroundColor: getClassificationColor(item.classification?.category || null)},
              ]}>
              <Text style={styles.badgeText}>
                {item.classification?.category || 'UNKNOWN'}
              </Text>
            </View>
          </TouchableOpacity>
        )}
        ListEmptyComponent={
          <View style={styles.empty}>
            <Text style={styles.emptyText}>
              No hay aplicaciones para mostrar. Ejecuta un análisis primero.
            </Text>
          </View>
        }
      />
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F5F5F5',
  },
  header: {
    backgroundColor: '#FFFFFF',
    padding: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#F0F0F0',
  },
  headerTitle: {
    fontSize: 18,
    fontWeight: '700',
    color: '#1A1A2E',
  },
  headerSubtitle: {
    fontSize: 13,
    color: '#999',
    marginTop: 4,
  },
  list: {
    padding: 16,
  },
  appCard: {
    backgroundColor: '#FFFFFF',
    borderRadius: 12,
    padding: 16,
    marginBottom: 12,
    flexDirection: 'row',
    alignItems: 'center',
    shadowColor: '#000',
    shadowOffset: {width: 0, height: 1},
    shadowOpacity: 0.05,
    shadowRadius: 4,
    elevation: 1,
  },
  appInfo: {
    flex: 1,
  },
  appName: {
    fontSize: 15,
    fontWeight: '600',
    color: '#1A1A2E',
    marginBottom: 4,
  },
  appPackage: {
    fontSize: 12,
    color: '#999',
    marginBottom: 6,
  },
  tagsRow: {
    flexDirection: 'row',
    gap: 6,
  },
  tag: {
    fontSize: 10,
    fontWeight: '600',
    color: '#666',
    backgroundColor: '#F0F0F0',
    paddingHorizontal: 6,
    paddingVertical: 2,
    borderRadius: 4,
  },
  badge: {
    borderRadius: 6,
    paddingHorizontal: 8,
    paddingVertical: 4,
  },
  badgeText: {
    fontSize: 11,
    fontWeight: '600',
    color: '#FFFFFF',
  },
  empty: {
    padding: 48,
    alignItems: 'center',
  },
  emptyText: {
    fontSize: 14,
    color: '#999',
    textAlign: 'center',
  },
});
