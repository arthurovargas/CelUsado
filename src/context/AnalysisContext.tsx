import React, {createContext, useContext, useState, useCallback, ReactNode} from 'react';
import type {PackageAnalysisResult} from '../modules/android/PackageAnalyzer';
import type {DeviceControlResult} from '../modules/android/DeviceControl';
import type {IntegrityResult} from '../modules/android/NativeIntegrity';
import type {RiskResult} from '../domain/rules';

interface AnalysisContextType {
  result: PackageAnalysisResult | null;
  controlResult: DeviceControlResult | null;
  integrityResult: IntegrityResult | null;
  riskResult: RiskResult | null;
  isAnalyzing: boolean;
  error: string | null;
  setAnalysisResult: (result: PackageAnalysisResult) => void;
  setControlResult: (result: DeviceControlResult) => void;
  setIntegrityResult: (result: IntegrityResult) => void;
  setRiskResult: (result: RiskResult) => void;
  setAnalyzing: (value: boolean) => void;
  setError: (error: string | null) => void;
  clearResults: () => void;
}

const AnalysisContext = createContext<AnalysisContextType | undefined>(undefined);

export const AnalysisProvider = ({children}: {children: ReactNode}) => {
  const [result, setResult] = useState<PackageAnalysisResult | null>(null);
  const [controlResult, setControlResultState] = useState<DeviceControlResult | null>(null);
  const [integrityResult, setIntegrityResultState] = useState<IntegrityResult | null>(null);
  const [riskResult, setRiskResultState] = useState<RiskResult | null>(null);
  const [isAnalyzing, setIsAnalyzing] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const setAnalysisResult = useCallback((data: PackageAnalysisResult) => {
    setResult(data);
  }, []);

  const setControlResult = useCallback((data: DeviceControlResult) => {
    setControlResultState(data);
  }, []);

  const setIntegrityResult = useCallback((data: IntegrityResult) => {
    setIntegrityResultState(data);
  }, []);

  const setRiskResult = useCallback((data: RiskResult) => {
    setRiskResultState(data);
  }, []);

  const setAnalyzing = useCallback((value: boolean) => {
    setIsAnalyzing(value);
  }, []);

  const clearResults = useCallback(() => {
    setResult(null);
    setControlResultState(null);
    setIntegrityResultState(null);
    setRiskResultState(null);
    setError(null);
  }, []);

  return (
    <AnalysisContext.Provider
      value={{
        result,
        controlResult,
        integrityResult,
        riskResult,
        isAnalyzing,
        error,
        setAnalysisResult,
        setControlResult,
        setIntegrityResult,
        setRiskResult,
        setAnalyzing,
        setError,
        clearResults,
      }}>
      {children}
    </AnalysisContext.Provider>
  );
};

export const useAnalysis = (): AnalysisContextType => {
  const context = useContext(AnalysisContext);
  if (!context) {
    throw new Error('useAnalysis must be used within an AnalysisProvider');
  }
  return context;
};
