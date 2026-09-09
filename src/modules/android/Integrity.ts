import { analyzeIntegrity, IntegrityResult } from './NativeIntegrity';
import { logger } from '../../utils/Logger';

const TAG = 'IntegrityService';

export async function getIntegrityAnalysis(): Promise<IntegrityResult> {
  try {
    logger.info(TAG, 'Starting integrity analysis');
    const result = await analyzeIntegrity();
    logger.info(TAG, `Integrity analysis complete: ${result.overallStatus}`);
    return result;
  } catch (error) {
    logger.error(TAG, `Integrity analysis failed: ${error}`);
    throw error;
  }
}
