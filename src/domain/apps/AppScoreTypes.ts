export type AppLevel = 'NONE' | 'LOW' | 'MEDIUM' | 'HIGH' | 'VERY_HIGH';

export interface AppFactor {
  label: string;
  points: number;
  source: 'CONTROL' | 'INTEGRITY' | 'PACKAGE';
  status: string;
}

export interface AppScore {
  packageName: string;
  name: string;
  score: number;
  level: AppLevel;
  factors: AppFactor[];
  classification: string;
}
