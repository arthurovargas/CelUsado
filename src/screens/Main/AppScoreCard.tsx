import React from 'react';
import {View, Text, TouchableOpacity, StyleSheet} from 'react-native';
import type {AppScore} from '../../domain/apps';
import {getLevelLabel, getLevelColor} from '../../domain/apps';
import {colors} from '../../theme/colors';

interface AppScoreCardProps {
  app: AppScore;
  onPress: () => void;
}

export const AppScoreCard = ({app, onPress}: AppScoreCardProps) => {
  const levelColor = getLevelColor(app.level);

  return (
    <TouchableOpacity style={styles.card} onPress={onPress} activeOpacity={0.7}>
      <View style={styles.header}>
        <View style={styles.info}>
          <Text style={styles.name} numberOfLines={1}>{app.name}</Text>
          <Text style={styles.package} numberOfLines={1}>{app.packageName}</Text>
        </View>
        <View style={styles.scoreSection}>
          <Text style={[styles.score, {color: levelColor}]}>{app.score}</Text>
          <View style={[styles.levelBadge, {backgroundColor: levelColor}]}>
            <Text style={styles.levelText}>{getLevelLabel(app.level)}</Text>
          </View>
        </View>
      </View>

      {app.factors.length > 0 && (
        <View style={styles.factors}>
          {app.factors.slice(0, 3).map((factor, idx) => (
            <View key={idx} style={styles.factorChip}>
              <Text style={styles.factorLabel}>{factor.label}</Text>
              <Text style={styles.factorPoints}>+{factor.points}</Text>
            </View>
          ))}
          {app.factors.length > 3 && (
            <Text style={styles.moreFactors}>+{app.factors.length - 3}</Text>
          )}
        </View>
      )}
    </TouchableOpacity>
  );
};

const styles = StyleSheet.create({
  card: {
    backgroundColor: colors.surface,
    borderRadius: 12,
    padding: 14,
    marginBottom: 10,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  info: {
    flex: 1,
  },
  name: {
    fontSize: 15,
    fontWeight: '700',
    color: colors.text,
  },
  package: {
    fontSize: 11,
    color: colors.textMuted,
    marginTop: 2,
  },
  scoreSection: {
    alignItems: 'flex-end',
    marginLeft: 12,
  },
  score: {
    fontSize: 24,
    fontWeight: '800',
  },
  levelBadge: {
    paddingHorizontal: 6,
    paddingVertical: 2,
    borderRadius: 4,
    marginTop: 4,
  },
  levelText: {
    fontSize: 9,
    fontWeight: '700',
    color: '#FFFFFF',
    letterSpacing: 0.5,
  },
  factors: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    marginTop: 8,
    gap: 4,
  },
  factorChip: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: colors.card,
    borderRadius: 4,
    paddingHorizontal: 6,
    paddingVertical: 2,
    gap: 4,
  },
  factorLabel: {
    fontSize: 10,
    color: colors.textSecondary,
  },
  factorPoints: {
    fontSize: 10,
    fontWeight: '700',
    color: colors.text,
  },
  moreFactors: {
    fontSize: 10,
    color: colors.textMuted,
    alignSelf: 'center',
  },
});
