import type {DiagnosticReport} from '../domain/report/ReportExporter';

const MAX_HISTORY_SIZE = 20;

interface HistoryEntry {
  id: string;
  timestamp: number;
  deviceModel: string;
  riskLevel: string;
  riskScore: number;
  summary: string;
  report: DiagnosticReport;
}

class DiagnosticsHistory {
  private entries: HistoryEntry[] = [];
  private static instance: DiagnosticsHistory;

  private constructor() {}

  static getInstance(): DiagnosticsHistory {
    if (!DiagnosticsHistory.instance) {
      DiagnosticsHistory.instance = new DiagnosticsHistory();
    }
    return DiagnosticsHistory.instance;
  }

  addEntry(report: DiagnosticReport): string {
    const id = `diag_${Date.now()}_${Math.random().toString(36).substring(2, 9)}`;

    const entry: HistoryEntry = {
      id,
      timestamp: Date.now(),
      deviceModel: report.device
        ? `${report.device.manufacturer} ${report.device.model}`
        : 'Desconocido',
      riskLevel: report.risk?.level || 'UNKNOWN',
      riskScore: report.risk?.score || 0,
      summary: report.risk?.recommendation || 'Sin análisis de riesgo',
      report,
    };

    this.entries.unshift(entry);

    if (this.entries.length > MAX_HISTORY_SIZE) {
      this.entries = this.entries.slice(0, MAX_HISTORY_SIZE);
    }

    return id;
  }

  getEntries(): HistoryEntry[] {
    return [...this.entries];
  }

  getEntryById(id: string): HistoryEntry | undefined {
    return this.entries.find(entry => entry.id === id);
  }

  deleteEntry(id: string): boolean {
    const index = this.entries.findIndex(entry => entry.id === id);
    if (index !== -1) {
      this.entries.splice(index, 1);
      return true;
    }
    return false;
  }

  clearAll(): void {
    this.entries = [];
  }

  getEntryCount(): number {
    return this.entries.length;
  }
}

export const diagnosticsHistory = DiagnosticsHistory.getInstance();
