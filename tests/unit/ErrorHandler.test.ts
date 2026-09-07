import {createAppError, handleError, setErrorHandler} from '../../src/utils/ErrorHandler';

describe('ErrorHandler', () => {
  it('creates an app error', () => {
    const error = createAppError('E001', 'Test error', 'test-source');
    expect(error.code).toBe('E001');
    expect(error.message).toBe('Test error');
    expect(error.source).toBe('test-source');
    expect(error.platform).toBe('ios');
    expect(error.timestamp).toBeGreaterThan(0);
  });

  it('handles error and calls handler', () => {
    const handler = jest.fn();
    setErrorHandler(handler);

    const error = handleError('E002', 'Something failed', 'module');
    expect(handler).toHaveBeenCalledWith(error);
    expect(error.code).toBe('E002');
  });

  it('handles original error with stack', () => {
    const handler = jest.fn();
    setErrorHandler(handler);

    const original = new Error('original error');
    const appError = handleError('E003', 'Wrapped', 'source', original);
    expect(appError.stack).toBeDefined();
  });
});
