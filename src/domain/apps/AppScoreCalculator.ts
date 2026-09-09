import type {PackageAnalysisResult, NormalizedPackage} from '../../modules/android/PackageAnalyzer';
import type {DeviceControlResult, DeviceControlIndicator} from '../../modules/android/DeviceControl';
import type {IntegrityResult} from '../../modules/android/NativeIntegrity';
import type {AppScore, AppFactor, AppLevel} from './AppScoreTypes';

function getLevel(score: number): AppLevel {
  if (score >= 70) return 'VERY_HIGH';
  if (score >= 40) return 'HIGH';
  if (score >= 15) return 'MEDIUM';
  if (score >= 1) return 'LOW';
  return 'NONE';
}

function getLevelLabel(level: AppLevel): string {
  switch (level) {
    case 'VERY_HIGH':
      return 'MUY ALTO';
    case 'HIGH':
      return 'ALTO';
    case 'MEDIUM':
      return 'MEDIO';
    case 'LOW':
      return 'BAJO';
    case 'NONE':
      return 'SIN CAPACIDADES';
  }
}

function getLevelColor(level: AppLevel): string {
  switch (level) {
    case 'VERY_HIGH':
      return '#C0392B';
    case 'HIGH':
      return '#E74C3C';
    case 'MEDIUM':
      return '#F39C12';
    case 'LOW':
      return '#3498DB';
    case 'NONE':
      return '#95A5A6';
  }
}

function findControlIndicators(
  packageName: string,
  control: DeviceControlResult,
): DeviceControlIndicator[] {
  return control.indicators.filter(
    ind =>
      ind.packageName === packageName &&
      (ind.status === 'CONFIRMED' || ind.status === 'ACTIVE' || ind.status === 'DECLARED'),
  );
}

function scoreFromControl(indicators: DeviceControlIndicator[]): {score: number; factors: AppFactor[]} {
  let score = 0;
  const factors: AppFactor[] = [];

  for (const ind of indicators) {
    let points = 0;

    switch (ind.type) {
      case 'DEVICE_OWNER':
        points = 40;
        break;
      case 'PROFILE_OWNER':
        points = 30;
        break;
      case 'DEVICE_ADMIN':
        points = 20;
        break;
      case 'ACCESSIBILITY_SERVICE':
        points = 15;
        break;
      case 'VPN_SERVICE':
        points = 10;
        break;
      case 'OVERLAY':
        points = 10;
        break;
      case 'FORCE_LOCK':
        points = 15;
        break;
      case 'WIPE_CAPABILITY':
        points = 20;
        break;
      case 'BOOT_RECEIVER':
        points = 5;
        break;
      case 'PERSISTENT_SERVICE':
        points = 10;
        break;
      case 'ADMINISTRATIVE_POLICY':
        points = 10;
        break;
      default:
        points = 5;
    }

    const alreadyHas = factors.some(f => f.label === ind.type);
    if (!alreadyHas) {
      score += points;
      factors.push({
        label: ind.type,
        points,
        source: 'CONTROL',
        status: ind.status,
      });
    }
  }

  return {score, factors};
}

function scoreFromPackage(pkg: NormalizedPackage): {score: number; factors: AppFactor[]} {
  let score = 0;
  const factors: AppFactor[] = [];

  if (pkg.indicators) {
    for (const ind of pkg.indicators) {
      if (ind === 'ROOT_BINARY' || ind === 'SU_BINARY' || ind === 'SUID_BINARY') {
        score += 35;
        factors.push({label: 'Root Binary', points: 35, source: 'PACKAGE', status: 'CONFIRMED'});
      } else if (ind === 'ROOT_APP') {
        score += 35;
        factors.push({label: 'Root App', points: 35, source: 'PACKAGE', status: 'CONFIRMED'});
      } else if (ind.includes('root') || ind.includes('su_binary')) {
        score += 35;
        factors.push({label: 'Root Indicator', points: 35, source: 'PACKAGE', status: 'DETECTED'});
      }
    }
  }

  return {score, factors};
}

function scoreFromIntegrity(
  _packageName: string,
  integrity: IntegrityResult,
): {score: number; factors: AppFactor[]} {
  let score = 0;
  const factors: AppFactor[] = [];

  for (const ind of integrity.indicators) {
    if (ind.status !== 'CONFIRMED' && ind.status !== 'ACTIVE') {
      continue;
    }

    const alreadyHas = factors.some(f => f.label === ind.type);
    if (alreadyHas) {
      continue;
    }

    if (ind.type === 'MODIFICATION_FRAMEWORK') {
      score += 35;
      factors.push({label: 'Mod Framework', points: 35, source: 'INTEGRITY', status: ind.status});
    } else if (ind.type === 'ROOT_BINARY' || ind.type === 'ROOT_APP') {
      score += 35;
      factors.push({label: 'Root App', points: 35, source: 'INTEGRITY', status: ind.status});
    }
  }

  return {score, factors};
}

export function calculateAllApps(
  packages: PackageAnalysisResult,
  control: DeviceControlResult,
  integrity: IntegrityResult,
): AppScore[] {
  const results: AppScore[] = [];

  for (const pkg of packages.packages) {
    const controlResult = scoreFromControl(
      findControlIndicators(pkg.packageName, control),
    );
    const packageResult = scoreFromPackage(pkg);
    const integrityResult = scoreFromIntegrity(pkg.packageName, integrity);

    const allFactors = [...controlResult.factors, ...packageResult.factors, ...integrityResult.factors];

    const totalScore = Math.min(
      controlResult.score + packageResult.score + integrityResult.score,
      100,
    );

    const level = getLevel(totalScore);

    results.push({
      packageName: pkg.packageName,
      name: pkg.label,
      score: totalScore,
      level,
      factors: allFactors,
      classification: pkg.classification?.category || 'UNKNOWN',
    });
  }

  results.sort((a, b) => b.score - a.score);

  return results;
}

export {getLevelLabel, getLevelColor};
