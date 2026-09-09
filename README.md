# CelUsado

> **"La herramienta técnica definitiva para la compra y venta segura de dispositivos Android."**

**CelUsado** es una aplicación de código abierto diseñada para realizar auditorías técnicas profundas en dispositivos Android antes de una transacción. A diferencia de las apps de diagnóstico tradicionales que solo prueban el hardware, CelUsado se enfoca en la **seguridad administrativa e integridad del software**, ayudando a identificar riesgos que no son visibles a simple vista.

## Características Principales

- **Detección de MDM y Control Remoto:** Identifica perfiles de *Device Owner*, *Profile Owner* y *Device Administrators* que podrían ser usados para bloquear el equipo remotamente (común en equipos financiados o corporativos).
- **Auditoría de Aplicaciones:** Clasifica las apps instaladas (Sistema, OEM, Google, Operador, Usuario) y detecta permisos sensibles como `FORCE_LOCK` o servicios de accesibilidad sospechosos.
- **Integridad del Sistema:** Busca indicios de modificaciones en el sistema, estado del bootloader y señales de Root.
- **Modo Oscuro:** Interfaz completamente oscura para mejor experiencia visual.
- **Privacidad Total:** Análisis pasivo y offline. La app no modifica el sistema ni envía datos personales a servidores externos.

---

## Sistema de Puntuación de Aplicaciones

Cada aplicación instalada recibe un **puntaje individual de 0 a 100** basado en las capacidades de riesgo que posee. Este puntaje se calcula sumando los pesos de los factores detectados en la aplicación.

### Fórmula de Puntuación

| Factor | Puntos | Descripción |
|---|---|---|
| **DEVICE_OWNER** | +40 | Control total del dispositivo. Puede configurar políticas, borrar datos, bloquear funciones y administrar otras apps. |
| **PROFILE_OWNER** | +30 | Control dentro de un perfil de trabajo. Puede restringir apps, configuración y datos dentro de ese perfil. |
| **DEVICE_ADMIN** | +20 | Privilegios de administrador. Puede bloquear el dispositivo, cambiar contraseña, borrar datos y restringir funciones. |
| **ROOT_BINARY** | +35 | Binario de root (su, suid) detectado en el sistema. Acceso de superusuario. |
| **ROOT_APP** | +35 | Aplicación de root conocida (Magisk, SuperSU). Dispositivo modificado para obtener acceso root. |
| **Mod Framework** | +35 | Framework de modificación (Xposed, LSPosed). Permite modificar el comportamiento del sistema y otras apps. |
| **WIPE_CAPABILITY** | +20 | Capacidad de borrar todos los datos del dispositivo (factory reset). |
| **FORCE_LOCK** | +15 | Puede bloquear el dispositivo forzosamente sin intervención del usuario. |
| **ACCESSIBILITY_SERVICE** | +15 | Servicio de accesibilidad de terceros habilitado. Puede leer pantalla, capturar gestos y controlar el dispositivo. |
| **VPN_SERVICE** | +10 | Servicio VPN activo. Puede interceptar y redirigir todo el tráfico de red. |
| **OVERLAY** | +10 | Permiso para dibujar sobre otras apps. Puede mostrar ventanas encima de otras aplicaciones (potencial phishing). |
| **PERSISTENT_SERVICE** | +10 | Servicio persistente que no se puede detener fácilmente. Se mantiene activo en segundo plano. |
| **BOOT_RECEIVER** | +5 | Se ejecuta automáticamente al encender el dispositivo. |
| **ADMINISTRATIVE_POLICY** | +5 | Aplica políticas administrativas que pueden restringir funciones del dispositivo. |

> **Nota:** El puntaje total está limitado a un máximo de 100.

### Niveles de Riesgo

| Nivel | Rango | Color | Significado |
|---|---|---|---|
| **SIN CAPACIDADES** | 0 | Gris | La aplicación no tiene capacidades especiales detectadas. |
| **BAJO** | 1 - 14 | Azul | Capacidades mínimas, generalmente inofensivas. |
| **MEDIO** | 15 - 39 | Amarillo | Capacidades que requieren revisión manual. |
| **ALTO** | 40 - 69 | Rojo claro | Evidencia fuerte de administración o control. |
| **MUY ALTO** | 70 - 100 | Rojo oscuro | Control total del dispositivo o acceso root. |

### Cómo se Presenta la Información

En la pantalla principal, cada aplicación muestra:

```
┌─────────────────────────────────────────┐
│  Seguridad                    65       │
│  com.miui.securitycenter      ALTO     │
│                                         │
│  [ACCESSIBILITY_SERVICE +15]            │
│  [BOOT_RECEIVER +5] [PERSISTENT +10]   │
└─────────────────────────────────────────┘
```

- **Nombre y Package:** Identificación de la aplicación
- **Puntaje:** Número de 0 a 100
- **Nivel:** Badge con color que indica el nivel de riesgo
- **Factores:** Los 3 factores principales que contribuyen al puntaje

### Clasificación de Aplicaciones

Las apps se clasifican automáticamente en categorías:

| Categoría | Descripción |
|---|---|
| **SYSTEM** | Apps del sistema Android (Settings, Phone, etc.) |
| **OEM** | Apps del fabricante (Samsung, Xiaomi, etc.) |
| **GOOGLE** | Apps de Google (Play Store, Gmail, Maps, etc.) |
| **CARRIER** | Apps del operador de telefonía |
| **USER** | Apps instaladas por el usuario |
| **UNKNOWN** | Apps no clasificadas |

---

## Pantalla de Detalle de Aplicación

Al tocar una aplicación, se muestra su detalle completo:

### Secciones

1. **Puntaje General:** Score /100 con nivel de riesgo
2. **Capacidades Detectadas:** Lista de factores con sus puntos. Toca cada factor para ver una explicación detallada.
3. **Información:** Package, versión, instalador, categoría, si es app del sistema.
4. **Permisos Declarados:** Lista completa de permisos. Toca para expandir y ver todos.

### Explicaciones de Capacidades

Cada capacidad detectada tiene una explicación detallada:

| Capacidad | Explicación |
|---|---|
| **DEVICE_OWNER** | Control total del dispositivo. Puede configurar políticas de seguridad, borrar datos, bloquear funciones y administrar otras aplicaciones. |
| **PROFILE_OWNER** | Control dentro de un perfil de trabajo. Puede restringir apps, configuración y datos dentro de ese perfil. |
| **DEVICE_ADMIN** | Privilegios de administrador. Puede bloquear el dispositivo, cambiar contraseña, borrar datos y restringir funciones. |
| **ACCESSIBILITY_SERVICE** | Servicio de accesibilidad de terceros habilitado. Puede leer el contenido de la pantalla, capturar gestos y controlar el dispositivo. |
| **VPN_SERVICE** | Servicio VPN activo. Puede interceptar y redirigir todo el tráfico de red del dispositivo. |
| **OVERLAY** | Permiso para dibujar sobre otras apps. Puede mostrar ventanas encima de otras aplicaciones, potencialmente para phishing. |
| **FORCE_LOCK** | Puede bloquear el dispositivo forzosamente sin intervención del usuario. |
| **WIPE_CAPABILITY** | Capacidad de borrar todos los datos del dispositivo (factory reset). |
| **BOOT_RECEIVER** | Se ejecuta automáticamente al encender el dispositivo. |
| **PERSISTENT_SERVICE** | Servicio persistente que no se puede detener fácilmente. |
| **ROOT_BINARY** | Binario de root detectado en el sistema. Acceso de superusuario. |
| **ROOT_APP** | Aplicación de root conocida detectada. Dispositivo modificado. |
| **Mod Framework** | Framework de modificación detectado. Permite modificar el comportamiento del sistema. |
| **ADMINISTRATIVE_POLICY** | Políticas administrativas activas que pueden restringir funciones. |

---

## Análisis de Integridad

El sistema verifica la integridad del dispositivo buscando:

| Indicador | Descripción |
|---|---|
| **BUILD_TEST_KEYS** | Build compilado con claves de prueba (modificado) |
| **CUSTOM_ROM** | ROM personalizada instalada |
| **ROOT_BINARY** | Binario de root encontrado en el sistema |
| **ROOT_APP** | Aplicación de root conocida |
| **BOOTLOADER_UNLOCKED** | Bootloader desbloqueado |
| **MOD_FRAMEWORK** | Framework de modificación (Xposed, LSPosed) |

### Estados de Integridad

| Estado | Significado |
|---|---|
| **NORMAL** | Sin indicadores de modificación |
| **INDICATOR** | Un solo indicador detectado |
| **MULTIPLE_INDICATORS** | Múltiples indicadores |
| **INTEGRITY_COMPROMISED** | Integridad comprometida |

---

## Motor de Reglas

CelUsado utiliza un motor de 15 reglas predefinidas que evalúan combinaciones de indicadores para detectar patrones de riesgo conocidos.

### Categorías de Reglas

- **Administración:** Device Owner, Profile Owner, Device Admin
- **Servicios Peligrosos:** Accessibility, VPN, Overlay
- **Control Destructivo:** Force Lock, Wipe
- **Persistencia:** Boot Receiver, Persistent Service
- **Root/Modificación:** Root binaries, frameworks de modificación

---

## Instalación y Ejecución

### Requisitos Previos

- **Node.js**: v18 o superior
- **Java JDK**: 17 (Recomendado para React Native 0.76+)
- **Android Studio**: Configurado con las herramientas de compilación y emulador

### Configuración del Entorno (Windows)

#### 1. Variables de Entorno

Define `ANDROID_HOME` apuntando a tu SDK:

```
ANDROID_HOME = C:\Users\TU_USUARIO\AppData\Local\Android\Sdk
```

Añade a tu `Path` la carpeta `platform-tools`:

```
%ANDROID_HOME%\platform-tools
```

#### 2. Archivo `local.properties`

Crea el archivo `android/local.properties` con la ruta al SDK:

```properties
sdk.dir=C\:\\Users\\TU_USUARIO\\AppData\\Local\\Android\\Sdk
```

#### 3. Arquitectura del Proyecto

La Nueva Arquitectura de React Native 0.76 se encuentra **desactivada** en `android/gradle.properties`:

```properties
newArchEnabled=false
```

### Ejecución

#### Paso 1: Instalar dependencias

```bash
npm install
```

#### Paso 2: Iniciar Metro Bundler

En una terminal:

```bash
npm start
```

#### Paso 3: Ejecutar en Android

En una segunda terminal (con emulador o celular conectado):

```bash
npm run android
```

### Solución de Problemas

#### Error: `Address already in use :::8081`

```powershell
Stop-Process -Id (Get-NetTCPConnection -LocalPort 8081).OwningProcess -Force
```

#### Error: `The package 'AppDeviceInfo' doesn't seem to be linked`

```bash
cd android && ./gradlew clean && cd .. && npm run android
```

#### Error de validación en Reanimated

```powershell
(Get-Content node_modules/react-native-reanimated/android/build.gradle) -replace 'minimalReactNativeVersion = 78', 'minimalReactNativeVersion = 76' | Set-Content node_modules/react-native-reanimated/android/build.gradle
```

---

## Seguridad y Privacidad

- **Análisis Offline:** Todos los datos se procesan localmente en el dispositivo.
- **Sin Envío de Datos:** La app no envía información a servidores externos.
- **Sin Modificación:** El análisis es pasivo y no modifica el sistema.
- **Git Ignore:** El archivo `android/local.properties` y carpetas de compilación están ignoradas para evitar exponer rutas locales.

---

## Stack Tecnológico

- **Frontend:** React Native 0.76 (TypeScript)
- **Capa Nativa:** Custom Modules en Kotlin para acceso profundo a APIs de Android
- **Arquitectura:** React Native Legacy Architecture (optimizado para compatibilidad con módulos nativos de seguridad)
