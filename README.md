# CelUsado
> **"La herramienta técnica definitiva para la compra y venta segura de dispositivos Android."**

**CelUsado** es una aplicación de código abierto diseñada para realizar auditorías técnicas profundas en dispositivos Android antes de una transacción. A diferencia de las apps de diagnóstico tradicionales que solo prueban el hardware, CelUsado se enfoca en la **seguridad administrativa e integridad del software**, ayudando a identificar riesgos que no son visibles a simple vista.

## ✨ Características Principales
*   **Detección de MDM y Control Remoto:** Identifica perfiles de *Device Owner*, *Profile Owner* y *Device Administrators* que podrían ser usados para bloquear el equipo remotamente (común en equipos financiados o corporativos).
*   **Auditoría de Aplicaciones:** Clasifica las apps instaladas (Sistema, OEM, Google, Operador, Usuario) y detecta permisos sensibles como `FORCE_LOCK` o servicios de accesibilidad sospechosos.
*   **Integridad del Sistema:** Busca indicios de modificaciones en el sistema, estado del bootloader y señales de Root.
*   **Hardware Real-Time:** Reporte detallado de SoC, salud de batería (ciclos y temperatura), almacenamiento y RAM real.
*   **Privacidad Total:** Análisis pasivo y offline. La app no modifica el sistema ni envía datos personales a servidores externos.

## 🛠️ Stack Tecnológico
*   **Frontend:** React Native 0.76 (TypeScript).
*   **Capa Nativa:** Custom Modules en **Kotlin** para acceso profundo a APIs de Android (`Build`, `StatFs`, `BatteryManager`, etc.).
*   **Arquitectura:** React Native Legacy Architecture (optimizado para compatibilidad total con módulos nativos de seguridad).

---

## 🚀 Guía de Inicio Rápido

### 📋 Requisitos Previos
Antes de comenzar, asegúrate de tener instalado:
- **Node.js**: v18 o superior.
- **Java JDK**: 17 (Recomendado para React Native 0.76+).
- **Android Studio**: Configurado con las herramientas de compilación y emulador.

### ⚙️ Configuración del Entorno (Windows)

#### 1. Variables de Envorno
Es fundamental que tu sistema reconozca las herramientas de Android:
- Define `ANDROID_HOME` apuntando a tu SDK (ej. `C:\Users\TU_USUARIO\AppData\Local\Android\Sdk`).
- Añade a tu `Path` la carpeta `platform-tools` (ej. `%ANDROID_HOME%\platform-tools`).

#### 2. Archivo `local.properties`
Este proyecto requiere que el archivo `android/local.properties` exista y tenga la ruta correcta al SDK:
```properties
sdk.dir=C\:\\Users\\TU_USUARIO\\AppData\\Local\\Android\\Sdk
```
*(Asegúrate de escapar los dos puntos `\:` y las barras invertidas `\\`).*

#### 3. Arquitectura del Proyecto
Para garantizar la compatibilidad con los módulos nativos personalizados (`AppDeviceInfo`), la **Nueva Arquitectura** de React Native 0.76 se encuentra **desactivada** en `android/gradle.properties`:
```properties
newArchEnabled=false
```

---

## 🏃 Ejecución

### Paso 1: Instalar dependencias
```bash
npm install
```

### Paso 2: Iniciar Metro Bundler
En una terminal:
```bash
npm start
```

### Paso 3: Ejecutar en Android
En una segunda terminal (con emulador o celular conectado):
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
