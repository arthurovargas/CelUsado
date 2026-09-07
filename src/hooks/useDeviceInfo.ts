import {useState, useEffect, useCallback} from 'react';
import {fetchDeviceInfo} from '../domain/device';
import type {DeviceInfoExtended} from '../domain/device';
import {logger} from '../utils';

const TAG = 'useDeviceInfo';

interface DeviceInfoState {
  data: DeviceInfoExtended | null;
  isLoading: boolean;
  error: string | null;
}

const initialState: DeviceInfoState = {
  data: null,
  isLoading: false,
  error: null,
};

export function useDeviceInfo() {
  const [state, setState] = useState<DeviceInfoState>(initialState);

  const load = useCallback(async () => {
    setState(prev => ({...prev, isLoading: true, error: null}));
    try {
      const info = await fetchDeviceInfo();
      setState({data: info, isLoading: false, error: null});
      logger.info(TAG, 'Device info loaded successfully');
    } catch (err) {
      const message =
        err instanceof Error ? err.message : 'Error al obtener información del dispositivo';
      setState(prev => ({...prev, isLoading: false, error: message}));
      logger.error(TAG, 'Failed to load device info', err);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

  return {
    ...state,
    reload: load,
  };
}
