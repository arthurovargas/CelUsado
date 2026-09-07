import React from 'react';
import {View, Text, FlatList, TouchableOpacity, StyleSheet, SafeAreaView} from 'react-native';
import {useNavigation} from '@react-navigation/native';
import type {NativeStackNavigationProp} from '@react-navigation/native-stack';
import type {RootStackParamList} from '../../types/navigation';

type NavigationProp = NativeStackNavigationProp<RootStackParamList, 'Applications'>;

const MOCK_APPS = [
  {packageName: 'com.example.app1', name: 'Ejemplo App 1', classification: 'USER'},
  {packageName: 'com.example.app2', name: 'Ejemplo App 2', classification: 'SYSTEM'},
  {packageName: 'com.example.app3', name: 'Ejemplo App 3', classification: 'OEM'},
];

export const ApplicationsScreen = () => {
  const navigation = useNavigation<NavigationProp>();

  return (
    <SafeAreaView style={styles.container}>
      <FlatList
        data={MOCK_APPS}
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
              <Text style={styles.appName}>{item.name}</Text>
              <Text style={styles.appPackage}>{item.packageName}</Text>
            </View>
            <View style={styles.badge}>
              <Text style={styles.badgeText}>{item.classification}</Text>
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
  },
  badge: {
    backgroundColor: '#F0F0F0',
    borderRadius: 6,
    paddingHorizontal: 8,
    paddingVertical: 4,
  },
  badgeText: {
    fontSize: 11,
    fontWeight: '600',
    color: '#666',
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
