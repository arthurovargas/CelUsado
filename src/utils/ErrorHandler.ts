import {Platform} from 'react-native';

export type AppError = {
  code: string;
  message: string;
  source: string;
  timestamp: number;
  platform: string;
  stack?: string;
};

const TAG = 'ErrorHandler';

let errorHandler: ((error: AppError) => void) | null = null;

export const setErrorHandler = (handler: (error: AppError) => void) => {
  errorHandler = handler;
};

export const createAppError = (
  code: string,
  message: string,
  source: string,
  stack?: string,
): AppError => ({
  code,
  message,
  source,
  timestamp: Date.now(),
  platform: Platform.OS,
  stack,
});

export const handleError = (
  code: string,
  message: string,
  source: string,
  originalError?: unknown,
): AppError => {
  const error = originalError instanceof Error ? originalError : new Error(String(originalError));

  const appError = createAppError(code, message, source, error.stack);

  if (errorHandler) {
    errorHandler(appError);
  }

  return appError;
};

export const setupGlobalErrorHandlers = () => {
  const originalHandler = ErrorUtils.getGlobalHandler();

  ErrorUtils.setGlobalHandler((error: Error, isFatal?: boolean) => {
    const appError = createAppError(
      isFatal ? 'FATAL_ERROR' : 'GLOBAL_ERROR',
      error.message,
      'global',
      error.stack,
    );

    if (errorHandler) {
      errorHandler(appError);
    }

    if (originalHandler) {
      originalHandler(error, isFatal);
    }
  });
};
