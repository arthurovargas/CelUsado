import React from 'react';
import {View, Text, TouchableOpacity, StyleSheet, ScrollView} from 'react-native';
import {colors} from '../../theme/colors';

type Category = 'ALL' | 'SYSTEM' | 'OEM' | 'GOOGLE' | 'CARRIER' | 'USER';

interface CategoryFiltersProps {
  selected: Category;
  onSelect: (category: Category) => void;
  counts: Record<Category, number>;
}

const CATEGORY_LABELS: Record<Category, string> = {
  ALL: 'Todas',
  SYSTEM: 'Sistema',
  OEM: 'OEM',
  GOOGLE: 'Google',
  CARRIER: 'Operador',
  USER: 'Usuario',
};

const CATEGORIES: Category[] = ['ALL', 'SYSTEM', 'OEM', 'GOOGLE', 'CARRIER', 'USER'];

export const CategoryFilters = ({selected, onSelect, counts}: CategoryFiltersProps) => {
  return (
    <View style={styles.container}>
      <ScrollView
        horizontal
        showsHorizontalScrollIndicator={false}
        contentContainerStyle={styles.scrollContent}>
        {CATEGORIES.map(cat => {
          const isSelected = selected === cat;
          const count = counts[cat] || 0;
          return (
            <TouchableOpacity
              key={cat}
              style={[styles.chip, isSelected && styles.chipSelected]}
              onPress={() => onSelect(cat)}
              activeOpacity={0.7}>
              <Text style={[styles.chipText, isSelected && styles.chipTextSelected]}>
                {CATEGORY_LABELS[cat]}
              </Text>
              {count > 0 && (
                <View style={[styles.count, isSelected && styles.countSelected]}>
                  <Text style={[styles.countText, isSelected && styles.countTextSelected]}>
                    {count}
                  </Text>
                </View>
              )}
            </TouchableOpacity>
          );
        })}
      </ScrollView>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    marginBottom: 12,
    maxHeight: 44,
  },
  scrollContent: {
    paddingRight: 16,
    gap: 8,
  },
  chip: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: colors.surface,
    borderRadius: 20,
    paddingHorizontal: 14,
    height: 36,
  },
  chipSelected: {
    backgroundColor: '#3498DB',
  },
  chipText: {
    fontSize: 13,
    fontWeight: '600',
    color: colors.textSecondary,
  },
  chipTextSelected: {
    color: '#FFFFFF',
  },
  count: {
    backgroundColor: colors.card,
    borderRadius: 10,
    paddingHorizontal: 6,
    marginLeft: 6,
    height: 18,
    justifyContent: 'center',
  },
  countSelected: {
    backgroundColor: 'rgba(255,255,255,0.3)',
  },
  countText: {
    fontSize: 11,
    fontWeight: '600',
    color: colors.textMuted,
  },
  countTextSelected: {
    color: '#FFFFFF',
  },
});
