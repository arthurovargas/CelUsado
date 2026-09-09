import React from 'react';
import {View, Text, StyleSheet} from 'react-native';
import {colors} from '../../theme/colors';

type StepStatus = 'pending' | 'running' | 'done' | 'error';

interface Step {
  id: string;
  label: string;
  status: StepStatus;
}

interface ProgressStepsProps {
  steps: Step[];
}

const getStatusIcon = (status: StepStatus) => {
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

export const ProgressSteps = ({steps}: ProgressStepsProps) => {
  const allDone = steps.every(s => s.status === 'done');
  const hasError = steps.some(s => s.status === 'error');

  if (allDone) {
    return null;
  }

  return (
    <View style={styles.container}>
      {steps.map(step => (
        <View key={step.id} style={styles.stepRow}>
          <Text
            style={[
              styles.icon,
              step.status === 'done' && styles.iconDone,
              step.status === 'running' && styles.iconRunning,
              step.status === 'error' && styles.iconError,
            ]}>
            {getStatusIcon(step.status)}
          </Text>
          <Text
            style={[
              styles.label,
              step.status === 'done' && styles.labelDone,
              step.status === 'running' && styles.labelRunning,
            ]}>
            {step.label}
          </Text>
        </View>
      ))}
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
  stepRow: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 8,
    borderBottomWidth: 1,
    borderBottomColor: colors.divider,
  },
  icon: {
    width: 24,
    fontSize: 16,
    color: colors.textMuted,
    textAlign: 'center',
  },
  iconDone: {
    color: '#27AE60',
  },
  iconRunning: {
    color: '#3498DB',
  },
  iconError: {
    color: '#E74C3C',
  },
  label: {
    fontSize: 14,
    color: colors.textMuted,
    flex: 1,
  },
  labelDone: {
    color: colors.text,
  },
  labelRunning: {
    color: '#3498DB',
    fontWeight: '500',
  },
});
