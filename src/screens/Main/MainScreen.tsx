import React, {useState, useEffect, useMemo, useCallback} from 'react';
import {View, FlatList, StyleSheet, SafeAreaView} from 'react-native';
import {useNavigation} from '@react-navigation/native';
import type {NativeStackNavigationProp} from '@react-navigation/native-stack';
import type {RootStackParamList} from '../../types/navigation';
import {useAnalysis} from '../../context/AnalysisContext';
import {useDeviceInfo} from '../../hooks/useDeviceInfo';
import {fetchDeviceInfo} from '../../domain/device';
import {analyzeAllPackages} from '../../modules/android/PackageAnalyzer';
import {analyzeDeviceControl} from '../../modules/android/DeviceControl';
import {getIntegrityAnalysis} from '../../modules/android/Integrity';
import {calculateAllApps} from '../../domain/apps';
import type {AppScore} from '../../domain/apps';
import {colors} from '../../theme/colors';
import {DeviceHeader} from './DeviceHeader';
import {ProgressSteps} from './ProgressSteps';
import {CategoryFilters} from './CategoryFilters';
import {AppScoreCard} from './AppScoreCard';
import {logger} from '../../utils';

type NavigationProp = NativeStackNavigationProp<RootStackParamList, 'Main'>;

type StepStatus = 'pending' | 'running' | 'done' | 'error';

type FilterCategory = 'ALL' | 'SYSTEM' | 'OEM' | 'GOOGLE' | 'CARRIER' | 'USER';

const INITIAL_STEPS = [
  {id: 'device', label: 'Información del equipo', status: 'pending' as StepStatus},
  {id: 'apps', label: 'Análisis de aplicaciones', status: 'pending' as StepStatus},
  {id: 'control', label: 'Análisis de administración', status: 'pending' as StepStatus},
  {id: 'integrity', label: 'Análisis de integridad', status: 'pending' as StepStatus},
];

const TAG = 'MainScreen';

export const MainScreen = () => {
  const navigation = useNavigation<NavigationProp>();
  const {data: deviceInfo} = useDeviceInfo();
  const {
    setAnalysisResult,
    setControlResult,
    setIntegrityResult,
    setRiskResult,
    result,
    controlResult,
    integrityResult,
  } = useAnalysis();

  const [steps, setSteps] = useState(INITIAL_STEPS);
  const [isAnalyzing, setIsAnalyzing] = useState(false);
  const [appScores, setAppScores] = useState<AppScore[]>([]);
  const [selectedCategory, setSelectedCategory] = useState<FilterCategory>('ALL');

  const updateStep = useCallback((index: number, status: StepStatus) => {
    setSteps(prev => prev.map((step, idx) => (idx === index ? {...step, status} : step)));
  }, []);

  useEffect(() => {
    const runAnalysis = async () => {
      if (appScores.length > 0) {
        return;
      }

      setIsAnalyzing(true);
      setSteps(INITIAL_STEPS);

      for (let i = 0; i < INITIAL_STEPS.length; i++) {
        updateStep(i, 'running');

        try {
          switch (INITIAL_STEPS[i]!.id) {
            case 'device':
              await fetchDeviceInfo();
              break;
            case 'apps':
              const packagesResult = await analyzeAllPackages();
              setAnalysisResult(packagesResult);
              logger.info(TAG, `Packages: ${packagesResult.packages.length}`);
              break;
            case 'control':
              const controlData = await analyzeDeviceControl();
              setControlResult(controlData);
              logger.info(TAG, `Control indicators: ${controlData.indicators.length}`);
              break;
            case 'integrity':
              const integrityData = await getIntegrityAnalysis();
              setIntegrityResult(integrityData);
              logger.info(TAG, `Integrity: ${integrityData.overallStatus}`);
              break;
          }
          updateStep(i, 'done');
        } catch (error) {
          logger.error(TAG, `Step ${INITIAL_STEPS[i]!.id} failed`, error);
          updateStep(i, 'error');
        }
      }

      setIsAnalyzing(false);
    };

    runAnalysis();
  }, []);

  useEffect(() => {
    if (result && controlResult && integrityResult && appScores.length === 0) {
      const scores = calculateAllApps(result, controlResult, integrityResult);
      setAppScores(scores);
      logger.info(TAG, `Calculated scores for ${scores.length} apps`);
    }
  }, [result, controlResult, integrityResult, appScores.length]);

  const filteredApps = useMemo(() => {
    if (selectedCategory === 'ALL') {
      return appScores;
    }
    return appScores.filter(app => app.classification === selectedCategory);
  }, [appScores, selectedCategory]);

  const categoryCounts = useMemo(() => {
    const counts: Record<FilterCategory, number> = {
      ALL: appScores.length,
      SYSTEM: 0,
      OEM: 0,
      GOOGLE: 0,
      CARRIER: 0,
      USER: 0,
    };
    for (const app of appScores) {
      const cat = app.classification as FilterCategory;
      if (cat in counts && cat !== 'ALL') {
        counts[cat]++;
      }
    }
    return counts;
  }, [appScores]);

  const handleAppPress = (app: AppScore) => {
    navigation.navigate('AppDetail', {packageName: app.packageName});
  };

  const allDone = steps.every(s => s.status === 'done');

  return (
    <SafeAreaView style={styles.container}>
      <View style={styles.content}>
        <DeviceHeader deviceInfo={deviceInfo} />

        <ProgressSteps steps={steps} />

        {allDone && appScores.length > 0 && (
          <>
            <CategoryFilters
              selected={selectedCategory}
              onSelect={setSelectedCategory}
              counts={categoryCounts}
            />
            <FlatList
              data={filteredApps}
              keyExtractor={item => item.packageName}
              renderItem={({item}) => (
                <AppScoreCard app={item} onPress={() => handleAppPress(item)} />
              )}
              contentContainerStyle={styles.list}
              showsVerticalScrollIndicator={false}
            />
          </>
        )}
      </View>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.background,
  },
  content: {
    flex: 1,
    padding: 16,
  },
  list: {
    paddingBottom: 20,
  },
});
