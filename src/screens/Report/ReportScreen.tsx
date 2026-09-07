import React from 'react';
import {View, Text, ScrollView, StyleSheet, SafeAreaView} from 'react-native';

export const ReportScreen = () => {
  return (
    <SafeAreaView style={styles.container}>
      <ScrollView contentContainerStyle={styles.content}>
        <View style={styles.header}>
          <Text style={styles.title}>INFORME DE DIAGNÓSTICO</Text>
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>DISPOSITIVO</Text>
          <Text style={styles.placeholder}>—</Text>
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>INTEGRIDAD</Text>
          <Text style={styles.placeholder}>Ejecuta un análisis para generar el informe.</Text>
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>ADMINISTRACIÓN</Text>
          <Text style={styles.placeholder}>—</Text>
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>APLICACIONES</Text>
          <Text style={styles.placeholder}>—</Text>
        </View>

        <View style={styles.section}>
          <Text style={styles.sectionTitle}>CONCLUSIÓN</Text>
          <Text style={styles.placeholder}>—</Text>
        </View>
      </ScrollView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#F5F5F5',
  },
  content: {
    padding: 24,
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
    marginBottom: 8,
  },
  placeholder: {
    fontSize: 14,
    color: '#999',
    fontStyle: 'italic',
  },
});
