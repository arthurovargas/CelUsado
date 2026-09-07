import {logger, LogLevel} from '../../src/utils/Logger';

describe('Logger', () => {
  beforeEach(() => {
    logger.clear();
  });

  it('logs info messages', () => {
    logger.info('TestTag', 'test message');
    const entries = logger.getEntries();
    expect(entries).toHaveLength(1);
    expect(entries[0]!.level).toBe(LogLevel.INFO);
    expect(entries[0]!.tag).toBe('TestTag');
    expect(entries[0]!.message).toBe('test message');
  });

  it('logs error messages with data', () => {
    logger.error('TestTag', 'error occurred', {code: 'E001'});
    const entries = logger.getEntries();
    expect(entries).toHaveLength(1);
    expect(entries[0]!.level).toBe(LogLevel.ERROR);
    expect(entries[0]!.data).toEqual({code: 'E001'});
  });

  it('filters entries by level', () => {
    logger.info('Tag1', 'info msg');
    logger.warn('Tag2', 'warn msg');
    logger.error('Tag3', 'error msg');

    const warns = logger.getEntriesByLevel(LogLevel.WARN);
    expect(warns).toHaveLength(1);
    expect(warns[0]!.message).toBe('warn msg');
  });

  it('filters entries by tag', () => {
    logger.info('TagA', 'msg1');
    logger.info('TagB', 'msg2');
    logger.info('TagA', 'msg3');

    const tagA = logger.getEntriesByTag('TagA');
    expect(tagA).toHaveLength(2);
  });

  it('clears entries', () => {
    logger.info('Tag', 'msg');
    logger.clear();
    expect(logger.getEntries()).toHaveLength(0);
  });

  it('exports as string', () => {
    logger.info('Tag', 'test');
    const exported = logger.exportAsString();
    expect(exported).toContain('[INFO][Tag] test');
  });
});
