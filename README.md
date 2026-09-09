# CelUsado
> **"La herramienta técnica definitiva para la compra y venta segura de dispositivos Android."**

**CelUsado** es una aplicación de código abierto diseñada para realizar auditorías técnicas profundas en dispositivos Android antes de una transacción. A diferencia de las apps de diagnóstico tradicionales que solo prueban el hardware, CelUsado se enfoca en la **seguridad administrativa e integridad del software**, ayudando a identificar riesgos que no son visibles a simple vista.

## ✨ Características Principales
*   **Detección de MDM y Control Remoto:** Identifica perfiles de *Device Owner*, *Profile Owner* y *Device Administrators* que podrían ser usados para bloquear el equipo remotamente (común en equipos financiados o corporativos).
*   **Auditoría de Aplicaciones:** Clasifica las apps instaladas (Sistema, OEM, Google, Operador, Usuario) y detecta permisos sensibles como `FORCE_LOCK` o servicios de accesibilidad sospechosos.
*   **Integridad del Sistema:** Busca indicios de modificaciones en el sistema, estado del bootloader y señales de Root.
*   **Motor de Reglas y Risk Score:** Evalúa combinación de indicadores para asignar un nivel de riesgo (BAJO / MODERADO / ALTO / CRÍTICO).
*   **Informe Exportable:** Genera reportes en JSON o texto plano con todos los hallazgos.
*   **Privacidad Total:** Análisis pasivo y offline. La app no modifica el sistema ni envía datos personales a servidores externos.

## 🛠️ Stack Tecnológico
*   **Frontend:** React Native 0.76 (TypeScript).
*   **Capa Nativa:** Custom Modules en **Kotlin** para acceso profundo a APIs de Android (`Build`, `StatFs`, `BatteryManager`, etc.).
*   **Arquitectura:** React Native Legacy Architecture (optimizado para compatibilidad total con módulos nativos de seguridad).

---

## 📊 Informe de Diagnóstico

El informe se genera automáticamente después de ejecutar el análisis completo. Se divide en las siguientes secciones:

### 1. RIESGO GENERAL

Muestra el nivel de riesgo calculado por el motor de reglas:

| Nivel | Significado |
|---|---|
| **BAJO** (0-14) | Dispositivo en estado estándar, sin indicadores significativos |
| **MODERADO** (15-34) | Señales que requieren verificación manual |
| **ALTO** (35-59) | Evidencia fuerte de administración o modificación |
| **CRÍTICO** (60-100) | Múltiples indicadores de riesgo组合 |

El score se calcula sumando pesos de reglas activadas (root, administración, integridad, etc.).

### 2. IDENTIFICACIÓN

Datos básicos del dispositivo:
- Fabricante, modelo y marca
- Versión de Android y nivel de SDK

### 3. HARDWARE

Especificaciones físicas:
- **SoC** (System on Chip): Procesador principal
- **Board**: Placa base
- **RAM**: Memoria total instalada
- **Almacenamiento**: Capacidad total del dispositivo
- **CPU**: Número de núcleos del procesador

### 4. SOFTWARE

Información del sistema operativo:
- **Versión**: Build completo del firmware
- **Parche de seguridad**: Fecha del último parche de Android
- **Fingerprint**: Identificador único del build
- **Bootloader**: Estado del bootloader
- **Build Tags**: `release-keys` (normal) vs `test-keys` (modificado)
- **Build Type**: `user` (normal) vs `userdebug`/`eng` (desarrollo)

### 5. COBERTURA - ANÁLISIS DE APLICACIONES

Resumen del análisis de paquetes:
- Total de paquetes descubiertos vs analizados
- Limitaciones de visibilidad (apps ocultas por el sistema)

### 6. COBERTURA - ANÁLISIS DE ADMINISTRACIÓN

Resumen del análisis de control:
- Número total de indicadores detectados
- Estado del análisis y limitaciones

### 7. INTEGRIDAD DEL DISPOSITIVO

Estado general de integridad del sistema:
- **NORMAL**: Sin indicadores de modificación
- **INDICATOR**: Un solo indicador detectado
- **MULTIPLE_INDICATORS**: Múltiples indicadores
- **INTEGRITY_COMPROMISED**: Integridad comprometida

### 8. INDICADORES DE INTEGRIDAD

Detalle de cada indicador de integridad detectado:

| Campo | Descripción |
|---|---|
| **Tipo** | BUILD_TEST_KEYS, CUSTOM_ROM, ROOT_BINARY, BOOTLOADER_UNLOCKED, etc. |
| **Estado** | CONFIRMED, ACTIVE, NOT_DETECTED, NOT_AVAILABLE |
| **Confianza** | 0-100% de certeza en la detección |
| **Fuente** | De dónde se obtuvo la información (propiedad del sistema, paquete, etc.) |

### 9. CLASIFICACIÓN DE APLICACIONES

Distribución de apps por categoría:

| Categoría | Descripción |
|---|---|
| **SYSTEM** | Apps del sistema Android (Settings, Phone, etc.) |
| **OEM** | Apps del fabricante (Samsung, Xiaomi, etc.) |
| **GOOGLE** | Apps de Google (Play Store, Gmail, Maps, etc.) |
| **CARRIER** | Apps del operador de telefonía |
| **USER** | Apps instaladas por el usuario |
| **UNKNOWN** | Apps no clasificadas |

### 10. INDICADORES DE ADMINISTRACIÓN

Lista de indicadores de control detectados. Cada indicador muestra:

| Campo | Descripción |
|---|---|
| **Tipo** | DEVICE_ADMIN, DEVICE_OWNER, PROFILE_OWNER, ACCESSIBILITY_SERVICE, VPN_SERVICE, OVERLAY, FORCE_LOCK, WIPE_CAPABILITY |
| **Paquete** | Nombre del paquete de la aplicación |
| **Estado** | CONFIRMED, ACTIVE, DECLARED, NOT_DETECTED, NOT_ACCESSIBLE |
| **Confianza** | 0-100% de certeza |

#### Nivel de Evidencia

Cada indicador tiene un badge de nivel de evidencia:

| Badge | Estado del indicador | Significado |
|---|---|---|
| **HECHO** | CONFIRMED, ACTIVE | Evidencia confirmada técnicamente |
| **INDICIO** | DECLARED, CAPABLE, DETECTED | Señal detectada pero no confirmada |
| **NO DETERMINABLE** | NOT_DETECTED, NOT_ACCESSIBLE, NOT_AVAILABLE | No se pudo verificar |

### 11. TODAS LAS APLICACIONES

Lista completa de apps analizadas con su clasificación.

### 12. CONCLUSIÓN

Resumen ejecutivo:
- Número de indicadores de alto nivel detectados
- Recomendación basada en el nivel de riesgo
- Disclaimer sobre la naturaleza técnica del informe

### 13. METADATOS DEL INFORME

- Fecha y hora de generación
- Versión de la aplicación (`0.0.1`)
- Versión de las reglas de análisis (`1.0.0`)
- Conteos de paquetes e indicadores

---

## 📱 Sección de Aplicaciones

La pantalla de aplicaciones muestra el detalle de cada paquete instalado:

### Información Básica

| Campo | Descripción |
|---|---|
| **Nombre** | Nombre visible de la aplicación |
| **Package** | Identificador único del paquete (ej: `com.whatsapp`) |
| **Versión** | Nombre y código de versión |
| **Instalador** | Paquete que instaló la app (Google Play, Samsung Store, etc.) |
| **Primera instalación** | Fecha de instalación original |
| **Última actualización** | Fecha de la última actualización |

### Estado

| Campo | Descripción |
|---|---|
| **Sistema** | `true` si es app del sistema |
| **Actualizada** | `true` si es una app del sistema que fue actualizada |
| **Debuggable** | `true` si permite depuración (normal en desarrollo) |

### Clasificación

| Campo | Descripción |
|---|---|
| **Categoría** | SYSTEM, OEM, GOOGLE, CARRIER, USER, UNKNOWN |
| **Confianza** | 0-100% de certeza en la clasificación |
| **Evidencia** | Razones de la clasificación |

### Componentes

| Campo | Descripción |
|---|---|
| **Permisos** | Permisos declarados en el manifest |
| **Servicios** | Servicios registrados |
| **Receptores** | Broadcast receivers registrados |
| **Activities** | Activities registradas |
| **Providers** | Content providers registrados |

### Firma

| Campo | Descripción |
|---|---|
| **Esquema** | Versión del esquema de firma |
| **Múltiples firmantes** | `true` si tiene más de un certificado |
| **Firmantes actuales** | Certificados actuales |
| **Firmantes históricos** | Certificados anteriores (cambios de firma = sospechoso) |

### Indicadores

Lista de indicadores detectados para la aplicación (si los hay).

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
