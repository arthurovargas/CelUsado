export enum LogLevel {
  DEBUG = 'DEBUG',
  INFO = 'INFO',
  WARN = 'WARN',
  ERROR = 'ERROR',
}

type LogEntry = {
  timestamp: number;
  level: LogLevel;
  tag: string;
  message: string;
  data?: unknown;
};

const MAX_LOG_ENTRIES = 1000;

class Logger {
  private entries: LogEntry[] = [];
  private static instance: Logger;

  private constructor() {}

  static getInstance(): Logger {
    if (!Logger.instance) {
      Logger.instance = new Logger();
    }
    return Logger.instance;
  }

  debug(tag: string, message: string, data?: unknown) {
    this.log(LogLevel.DEBUG, tag, message, data);
  }

  info(tag: string, message: string, data?: unknown) {
    this.log(LogLevel.INFO, tag, message, data);
  }

  warn(tag: string, message: string, data?: unknown) {
    this.log(LogLevel.WARN, tag, message, data);
  }

  error(tag: string, message: string, data?: unknown) {
    this.log(LogLevel.ERROR, tag, message, data);
  }

  private log(level: LogLevel, tag: string, message: string, data?: unknown) {
    const entry: LogEntry = {
      timestamp: Date.now(),
      level,
      tag,
      message,
      data,
    };

    this.entries.push(entry);

    if (this.entries.length > MAX_LOG_ENTRIES) {
      this.entries = this.entries.slice(-MAX_LOG_ENTRIES);
    }

    if (__DEV__) {
      const prefix = `[${level}][${tag}]`;
      switch (level) {
        case LogLevel.DEBUG:
          console.debug(prefix, message, data ?? '');
          break;
        case LogLevel.INFO:
          console.log(prefix, message, data ?? '');
          break;
        case LogLevel.WARN:
          console.warn(prefix, message, data ?? '');
          break;
        case LogLevel.ERROR:
          console.error(prefix, message, data ?? '');
          break;
      }
    }
  }

  getEntries(): LogEntry[] {
    return [...this.entries];
  }

  getEntriesByLevel(level: LogLevel): LogEntry[] {
    return this.entries.filter(entry => entry.level === level);
  }

  getEntriesByTag(tag: string): LogEntry[] {
    return this.entries.filter(entry => entry.tag === tag);
  }

  clear() {
    this.entries = [];
  }

  exportAsString(): string {
    return this.entries
      .map(entry => {
        const date = new Date(entry.timestamp).toISOString();
        const dataStr = entry.data ? ` | ${JSON.stringify(entry.data)}` : '';
        return `${date} [${entry.level}][${entry.tag}] ${entry.message}${dataStr}`;
      })
      .join('\n');
  }
}

export const logger = Logger.getInstance();
