# CelUsado - Diagnóstico y Verificación Android

Aplicación de React Native diseñada para la revisión técnica de dispositivos Android usados, enfocada en detectar configuraciones de administración (MDM), integridad del sistema y capacidades de control.

## 🛠️ Requisitos Previos

Antes de comenzar, asegúrate de tener instalado:
- **Node.js**: v18 o superior.
- **Java JDK**: 17 (Recomendado para React Native 0.76+).
- **Android Studio**: Configurado con las herramientas de compilación y emulador.

## ⚙️ Configuración del Entorno (Windows)

### 1. Variables de Entorno
Es fundamental que tu sistema reconozca las herramientas de Android:
- Define `ANDROID_HOME` apuntando a tu SDK (ej. `C:\Users\TU_USUARIO\AppData\Local\Android\Sdk`).
- Añade a tu `Path` la carpeta `platform-tools` (ej. `%ANDROID_HOME%\platform-tools`).

### 2. Archivo `local.properties`
Este proyecto requiere que el archivo `android/local.properties` exista y tenga la ruta correcta al SDK para poder compilar:
```properties
sdk.dir=C\:\\Users\\TU_USUARIO\\AppData\\Local\\Android\\Sdk
```
*(Asegúrate de escapar los dos puntos `\:` y las barras invertidas `\\` como se muestra arriba).*

### 3. Arquitectura del Proyecto
Para garantizar la compatibilidad con los módulos nativos personalizados (`AppDeviceInfo`), la **Nueva Arquitectura** de React Native 0.76 se encuentra **desactivada** en `android/gradle.properties`:
```properties
newArchEnabled=false
```

## 🚀 Inicio Rápido

### Paso 1: Instalar dependencias
```bash
npm install
```

### Paso 2: Iniciar Metro Bundler
Abre una terminal y ejecuta:
```bash
npm start
```

### Paso 3: Ejecutar en Android
Abre una **segunda terminal** y con un emulador o celular conectado:
```bash
npm run android
```

## ⚠️ Solución de Problemas Comunes

### Error: `Address already in use :::8081`
Si el puerto 8081 está ocupado, ejecuta en PowerShell para liberarlo:
```powershell
Stop-Process -Id (Get-NetTCPConnection -LocalPort 8081).OwningProcess -Force
```

### Error: `The package 'AppDeviceInfo' doesn't seem to be linked`
Esto ocurre si intentas ejecutar la app sin haberla compilado después de cambios en Kotlin. Ejecuta:
```bash
cd android && ./gradlew clean && cd .. && npm run android
```

### Error de validación en Reanimated (minimalReactNativeVersion = 78)
Si reinstalas `node_modules`, es posible que Reanimated pida una versión de RN inexistente. Aplica este parche en PowerShell:
```powershell
(Get-Content node_modules/react-native-reanimated/android/build.gradle) -replace 'minimalReactNativeVersion = 78', 'minimalReactNativeVersion = 76' | Set-Content node_modules/react-native-reanimated/android/build.gradle
```

---

## 🔒 Seguridad y Git
El archivo `android/local.properties` y las carpetas de compilación están ignoradas en el `.gitignore` para evitar exponer rutas locales y mantener el repositorio ligero. No subas archivos `.keystore` al repositorio público.
