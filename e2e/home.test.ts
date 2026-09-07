import {by, device, element, expect} from 'detox';

describe('Home Screen', () => {
  beforeAll(async () => {
    await device.launchApp();
  });

  beforeEach(async () => {
    await device.reloadReactNative();
  });

  it('should display the app title', async () => {
    await expect(element(by.text('CelUsado'))).toBeVisible();
  });

  it('should display the scan button', async () => {
    await expect(element(by.text('Analizar equipo'))).toBeVisible();
  });

  it('should navigate to scan screen', async () => {
    await element(by.text('Analizar equipo')).tap();
    await expect(element(by.text('Iniciar análisis'))).toBeVisible();
  });
});
