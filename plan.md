# Plan de desarrollo — App de diagnóstico y verificación de dispositivos Android

## 1. Objetivo

Construir una aplicación Android orientada a la revisión de teléfonos usados antes de una compra, capaz de:

- Identificar el dispositivo y su información básica.
- Analizar las aplicaciones instaladas.
- Diferenciar, con niveles de confianza, aplicaciones del sistema/fabricante, Google, operador y terceros.
- Detectar aplicaciones con capacidades administrativas o de control del dispositivo.
- Detectar indicadores relacionados con `Device Administrator`, `Device Owner`, `Profile Owner`, `Accessibility`, `FORCE_LOCK`, servicios persistentes, receivers de arranque y otros permisos/capacidades relevantes.
- Generar un informe comprensible con indicadores, evidencias y nivel de riesgo.
- Evitar afirmar que un equipo está financiado, bloqueado o reportado como robado cuando los datos disponibles solamente constituyen indicios.

## 2. Principios del proyecto

1. **Privacidad primero:** recopilar únicamente los datos necesarios para el diagnóstico.
2. **Análisis pasivo:** la app debe detectar capacidades e indicadores, no intentar bloquear, modificar o desactivar otras aplicaciones.
3. **Evidencia antes que conclusiones:** cada alerta debe indicar qué evidencia la produjo.
4. **Separar capacidad de asociación:** una app con capacidad de bloqueo no implica que pertenezca a una financiera.
5. **Separar financiación de reporte de robo:** un equipo financiado no debe clasificarse automáticamente como robado.
6. **Compatibilidad Android:** asumir que algunas APIs están restringidas según versión, fabricante, rol de la aplicación y privilegios.
7. **Google Play:** diseñar desde el principio considerando las restricciones de visibilidad de paquetes y permisos.
8. **Resultados con incertidumbre:** usar estados como `DETECTADO`, `NO DETECTADO`, `NO DISPONIBLE` y `NO SE PUEDE DETERMINAR`.
9. **Offline-first:** la app debe funcionar completamente sin conexión. El backend es complementario, no requerido.

---

## 2b. Compatibilidad OEM

Cada fabricante tiene comportamientos y apps de sistema propias. Mantener una matriz de compatibilidad:

| Fabricante | Paquetes de sistema relevantes | Notas |
|------------|-------------------------------|-------|
| Samsung | `com.samsung.android.knox.*`, `com.sec.*` | Knox tiene su propio framework de administración |
| Xiaomi | `com.miui.*`, `com.xiaomi.*` | MIUI tiene permisos adicionales no estándar |
| Huawei | `com.huawei.*`, `com.hicore.*` | EMUI/HarmonyOS puede ocultar info |
| OnePlus | `com.oneplus.*` | Cerca de stock Android |
| Motorola | `com.motorola.*` | Poco stock, comportamiento estándar |
| LG | `com.lge.*` | Puede tener APIs propias |
| Pixel | `com.google.*` | Referencia AOSP, más predecible |

**Consideraciones:**

- Algunos fabricantes restringen APIs adicionales más allá de AOSP
- Las apps de sistema varían significativamente entre fabricantes
- La clasificación OEM/Google/Carrier puede requerir bases de datos específicas por fabricante
- Probar en al menos 2-3 fabricantes antes de generalizar resultados

---

## 3. Arquitectura tecnológica

### Cliente

- React Native
- TypeScript
- React Native New Architecture
- Jetpack/Android APIs mediante módulos nativos Kotlin
- Jetpack Compose solo si posteriormente se decide implementar pantallas nativas específicas
- SQLite para cache/base local

### Integración nativa

Crear módulos Kotlin mediante Turbo Native Modules.

Módulos iniciales:

- `PackageAnalyzer`
- `PermissionAnalyzer`
- `DeviceAdminAnalyzer`
- `DevicePolicyAnalyzer`
- `AccessibilityAnalyzer`
- `SystemAppAnalyzer`
- `IntegrityAnalyzer`
- `TelephonyAnalyzer`
- `DeviceInfoAnalyzer`

### Backend

Inicialmente puede omitirse para acelerar el MVP.

Cuando sea necesario:

- NestJS
- PostgreSQL
- API REST
- Sistema de actualización de reglas
- Registro de versiones de reglas

---

## 4. Arquitectura general

```text
┌────────────────────────────────────────────┐
│              React Native                  │
│                                            │
│ UI / navegación / estado / informes        │
│ TypeScript                                 │
└──────────────────────┬─────────────────────┘
                       │
              Turbo Native Modules
                       │
┌──────────────────────▼─────────────────────┐
│                 Kotlin                     │
│                                            │
│ PackageAnalyzer                            │
│ PermissionAnalyzer                         │
│ DeviceAdminAnalyzer                        │
│ DevicePolicyAnalyzer                       │
│ AccessibilityAnalyzer                     │
│ SystemAppAnalyzer                          │
│ IntegrityAnalyzer                          │
│ TelephonyAnalyzer                          │
│ DeviceInfoAnalyzer                         │
└──────────────────────┬─────────────────────┘
                       │
                       ▼
             Normalización de datos
                       │
                       ▼
               Motor de diagnóstico
                       │
             ┌─────────┴─────────┐
             ▼                   ▼
        Reglas locales       Backend/API
             │                   │
             └─────────┬─────────┘
                       ▼
                  Informe final
```

---

# 5. Fase 0 — Investigación técnica

## Objetivo

Determinar exactamente qué información puede obtener una aplicación Android normal en las versiones objetivo.

### Investigar

- `PackageManager`
- Package visibility
- `QUERY_ALL_PACKAGES`
- `ApplicationInfo`
- `PackageInfo`
- permisos declarados
- servicios
- broadcast receivers
- activities
- content providers
- firmas/certificados
- `DevicePolicyManager`
- Device Administrator
- Device Owner
- Profile Owner
- Accessibility Services
- Usage/overlay/VPN capabilities
- `TelephonyManager`
- Android Keystore
- Play Integrity
- bootloader/root indicators

### Versiones objetivo

Inicialmente:

- Android 13
- Android 14
- Android 15
- Android 16

Mantener una matriz de compatibilidad:

```text
Función | Android | Permiso | Rol requerido | Resultado
```

### Entregable

`docs/android-capabilities.md`

---

# 6. Fase 1 — Inicialización del proyecto

## Objetivo

Crear la base React Native.

### Tareas

- Crear proyecto React Native + TypeScript.
- Activar New Architecture.
- Configurar Android.
- Configurar ESLint.
- Configurar Prettier.
- Configurar TypeScript estricto.
- Configurar Git.
- Configurar Jest (unit testing).
- Configurar Detox (E2E testing).
- Crear estructura de carpetas.
- Crear sistema básico de navegación.
- Crear pantalla inicial.
- Crear sistema de logs.
- Crear manejo centralizado de errores.

### Estructura propuesta

```text
src/
├── components/
├── screens/
│   ├── Home/
│   ├── Scan/
│   ├── Applications/
│   ├── ApplicationDetail/
│   ├── Device/
│   └── Report/
├── modules/
│   └── android/
├── domain/
│   ├── device/
│   ├── applications/
│   ├── security/
│   └── financing/
├── services/
├── rules/
├── storage/
├── utils/
└── types/
```

---

# 7. Fase 2 — Módulo de información del dispositivo

## Objetivo

Obtener información básica.

### Datos

- fabricante
- modelo
- marca
- Android
- SDK
- versión/build
- hardware
- SoC cuando esté disponible
- RAM
- almacenamiento
- arquitectura CPU
- estado de batería
- operador cuando esté disponible

### Resultado

```json
{
  "manufacturer": "Samsung",
  "model": "SM-A566B",
  "androidVersion": "16",
  "sdk": 36
}
```

---

# 8. Fase 3 — Analizador de aplicaciones

## Objetivo

Construir el núcleo de recolección y estructuración de evidencia del diagnóstico. La Fase 3 debe responder **qué aplicaciones pueden observarse y qué características técnicas presentan**, sin convertir todavía esos datos en conclusiones de riesgo.

La Fase 3 debe separar estrictamente:

```text
DESCUBRIMIENTO → NORMALIZACIÓN → ANÁLISIS → EVIDENCIA → CLASIFICACIÓN
```

La interpretación de riesgo corresponde a fases posteriores.

### 8.1 Arquitectura interna

Dividir el `PackageAnalyzer` en responsabilidades independientes:

```text
PackageAnalyzer
│
├── PackageDiscovery
│   ├── LauncherDiscovery
│   ├── DeviceAdminDiscovery
│   ├── AccessibilityDiscovery
│   ├── VPNDiscovery
│   └── KnownPackageDiscovery
│
├── PackageMetadataReader
├── ComponentAnalyzer
├── PermissionAnalyzer
├── SignatureAnalyzer
├── EvidenceBuilder
└── PackageClassifier
```

Estos componentes pueden implementarse como clases Kotlin internas; no es necesario convertirlos en módulos React Native independientes.

### 8.2 Descubrimiento de paquetes

Construir primero un conjunto normalizado de `packageName` obtenido mediante diferentes fuentes:

```text
                 Package Discovery
                        │
        ┌───────────────┼────────────────┐
        │               │                │
     Launcher       Admin/API       Known packages
        │               │                │
        └───────────────┼────────────────┘
                        ▼
                 Set<String> packageNames
                        │
                        ▼
                  Eliminar duplicados
                        │
                        ▼
                  Analizar cada paquete
```

No tratar todos los intents como mecanismos equivalentes de enumeración. Por ejemplo, `ACTION_MAIN + CATEGORY_LAUNCHER` sirve para descubrir aplicaciones con launcher, mientras que la existencia de una pantalla de configuración de Accessibility o VPN no debe interpretarse por sí sola como una enumeración completa de esos servicios.

Utilizar APIs y mecanismos específicos para cada capacidad cuando estén disponibles y documentar las restricciones de cada uno.

### 8.3 Visibilidad de paquetes (Android 11+)

Desde Android 11, la visibilidad de paquetes está restringida y `QUERY_ALL_PACKAGES` tiene restricciones adicionales para aplicaciones distribuidas mediante Google Play.

La aplicación debe diferenciar explícitamente:

```text
DETECTADA
NO DETECTADA
NO VISIBLE
NO DISPONIBLE
NO SE PUEDE DETERMINAR
```

**No encontrada no significa necesariamente no instalada.**

Una aplicación sin launcher, sin un intent relevante o fuera del alcance permitido por las APIs puede no aparecer en el conjunto observable.

La aplicación debe mostrar la cobertura del análisis y nunca presentar una enumeración parcial como si fuera una lista completa de aplicaciones instaladas.

### 8.4 Datos por aplicación

Para cada paquete observable intentar obtener:

#### Nivel 1 — Identidad

- package name
- nombre visible
- versión
- version code

#### Nivel 2 — Origen y estado

- instalación/actualización cuando esté disponible
- aplicación del sistema
- aplicación del sistema actualizada
- instalador
- flags relevantes
- firmas/certificados

#### Nivel 3 — Componentes y capacidades

- permisos declarados
- servicios
- receivers
- activities relevantes
- proveedores
- componentes administrativos
- accessibility service
- otros indicadores disponibles

### 8.5 Fuente del descubrimiento

Guardar explícitamente cómo fue descubierta cada aplicación:

```json
{
  "discoverySources": [
    "LAUNCHER",
    "DEVICE_ADMIN",
    "ACCESSIBILITY",
    "KNOWN_PACKAGE"
  ]
}
```

Una aplicación descubierta mediante varias fuentes proporciona más contexto para las fases posteriores que una aplicación descubierta únicamente mediante launcher.

### 8.6 Modelo normalizado de aplicación

El resultado debe estructurarse para que las fases posteriores no tengan que volver a inspeccionar el dispositivo:

```json
{
  "packageName": "com.example.app",
  "label": "Example App",
  "version": {
    "name": "5.2.1",
    "code": 52100
  },
  "flags": {
    "system": true,
    "updatedSystem": false,
    "debuggable": false
  },
  "discoverySources": ["LAUNCHER"],
  "classification": {
    "category": "OEM",
    "confidence": 0.96,
    "evidence": [
      "SYSTEM_FLAG",
      "PACKAGE_PREFIX"
    ]
  },
  "permissions": [],
  "services": [],
  "receivers": [],
  "activities": [],
  "providers": [],
  "signing": {
    "certificates": []
  },
  "indicators": []
}
```

### 8.7 Evidencia y `confidence`

La clasificación debe incluir tanto `confidence` como las evidencias que justifican el resultado.

```json
{
  "category": "OEM",
  "confidence": 0.97,
  "evidence": [
    "SYSTEM_FLAG",
    "PACKAGE_PREFIX",
    "KNOWN_OEM_CERTIFICATE"
  ]
}
```

No utilizar un `confidence` aislado como una conclusión de caja negra.

### 8.8 Clasificación

Cada aplicación debe clasificarse como:

```text
SYSTEM
OEM
GOOGLE
CARRIER
USER
UNKNOWN
```

No asumir que `FLAG_SYSTEM` equivale automáticamente a `OEM`.

La clasificación debe utilizar una jerarquía de evidencias, por ejemplo:

```text
1. Firma/certificado conocido
        ↓
2. Package name conocido
        ↓
3. Origen/ubicación del paquete cuando esté disponible
        ↓
4. Flags del sistema
        ↓
5. Fabricante
        ↓
6. Base de reglas
        ↓
7. UNKNOWN
```

La clasificación de una aplicación debe permanecer separada de la evaluación de riesgo.

### 8.9 Separación entre clasificación y riesgo

`PackageClassifier` determina qué parece ser una aplicación y con qué confianza.

No debe determinar todavía si el dispositivo es de alto riesgo.

Por ejemplo:

```text
com.samsung.knox
→ OEM
→ confidence 0.99
```

no implica automáticamente `RIESGO ALTO`.

En cambio, una aplicación de terceros que presente simultáneamente indicadores como `DEVICE_ADMIN`, `FORCE_LOCK`, `ACCESSIBILITY` y `BOOT_COMPLETED` podrá proporcionar evidencia relevante para la Fase 4.

### 8.10 Cobertura del análisis

Añadir al resultado global una métrica explícita de cobertura:

```json
{
  "coverage": {
    "status": "PARTIAL",
    "packagesDiscovered": 87,
    "packagesAnalyzed": 87,
    "visibilityLimitations": true,
    "limitations": [
      "PACKAGE_VISIBILITY"
    ]
  }
}
```

Estados posibles:

```text
COMPLETE
PARTIAL
LIMITED
UNKNOWN
```

`COMPLETE` debe utilizarse con cautela y solamente cuando pueda justificarse técnicamente.

### 8.11 Salida de la Fase 3

La salida de esta fase debe ser un **modelo normalizado de aplicaciones y evidencias**, no un diagnóstico final.

Debe permitir que la Fase 4 consuma directamente:

```text
Aplicaciones observadas
        ↓
Metadatos
        ↓
Componentes
        ↓
Permisos
        ↓
Firmas
        ↓
Fuentes de descubrimiento
        ↓
Evidencias
        ↓
Clasificación + confidence
        ↓
Cobertura y limitaciones
```

### 8.12 PoC específica de la Fase 3

Antes de implementar la Fase 3 completa, realizar una PoC en un dispositivo físico que compruebe:

```text
1. Descubrimiento de paquetes
2. PackageInfo
3. ApplicationInfo
4. Permisos
5. Services
6. Receivers
7. Activities
8. Providers
9. Installer
10. Signatures
11. Flags
12. Clasificación
13. Evidencias
14. Cobertura
```

La PoC debe generar inicialmente un JSON crudo y registrar para cada aplicación:

```text
¿Fue descubierta?
¿Fue analizada?
¿Por qué mecanismo?
¿Qué información estuvo disponible?
¿Qué información no estuvo disponible?
```

### 8.13 Casos de prueba mínimos

La PoC debe probar como mínimo:

- aplicación normal con launcher;
- aplicación sin launcher;
- aplicación de sistema;
- aplicación de sistema actualizada;
- aplicación OEM;
- aplicación Google;
- aplicación de operador;
- aplicación con Device Administrator;
- aplicación con Accessibility Service;
- aplicación MDM;
- aplicación desconocida.

### Resultado esperado de la Fase 3

La Fase 3 debe responder con precisión:

> **Qué aplicaciones y componentes puede observar la aplicación, qué características técnicas presentan, mediante qué mecanismo fueron descubiertos y qué evidencias respaldan su clasificación.**

No debe afirmar por sí sola que un equipo está financiado, bloqueado, reportado como robado o que una aplicación es maliciosa.

---

# 9. Fase 4 — ANÁLISIS DE ADMINISTRACIÓN Y CONTROL DEL DISPOSITIVO

## 1. Objetivo

Implementar una capa de análisis que identifique **capacidades, configuraciones e indicadores relacionados con la administración, restricción o control del dispositivo**.

Esta fase debe consumir prioritariamente los resultados producidos por la Fase 3 y complementar únicamente la información que requiera consultas específicas al sistema.

La Fase 4 debe detectar y estructurar evidencia relacionada con:

* Device Administrator.
* Device Owner.
* Profile Owner.
* Device Policy Controller.
* políticas administrativas.
* capacidades relacionadas con bloqueo del dispositivo.
* capacidades relacionadas con wipe/reset administrativo.
* Accessibility Services.
* VPN.
* overlay.
* servicios persistentes.
* receivers de arranque.
* permisos y componentes relevantes para administración o control.

La Fase 4 **no debe determinar todavía el riesgo final del dispositivo o de una aplicación**.

Su responsabilidad es producir evidencia técnica estructurada para que fases posteriores puedan interpretarla.

---

# 2. Principio fundamental

La Fase 4 debe distinguir siempre entre:

```text
DECLARADO
    ≠
CAPAZ
    ≠
ACTIVO
    ≠
CONFIRMADO
```

Por ejemplo:

```text
Una aplicación declara un DeviceAdminReceiver
        ↓
DECLARADO / CAPAZ

NO implica necesariamente
        ↓
ACTIVO
```

Una aplicación puede tener la capacidad técnica de actuar como administrador sin estar actualmente activa como tal.

Por tanto, la ausencia de confirmación directa tampoco debe interpretarse automáticamente como ausencia de la capacidad.

---

# 3. Dependencia de la Fase 3

La Fase 4 debe reutilizar el resultado normalizado de la Fase 3.

La Fase 3 ya es responsable de obtener, cuando estén disponibles:

```text
packageName
label
version
flags
permissions
services
receivers
activities
providers
signatures
classification
classificationEvidence
discoverySources
coverage
```

La Fase 4 no debe duplicar:

* descubrimiento general de paquetes;
* lectura básica de metadatos;
* clasificación SYSTEM/OEM/GOOGLE/CARRIER/USER/UNKNOWN;
* análisis general de firmas ya realizado.

La arquitectura debe seguir el flujo:

```text
                    FASE 3
                       │
                       ▼
              Normalized App Model
                       │
                       ▼
                    FASE 4
                       │
       ┌───────────────┼────────────────┐
       │               │                │
 Administración     Control        Indicadores
       │               │                │
       ▼               ▼                ▼
 Device Admin     Accessibility    BOOT_COMPLETED
 Device Owner     VPN              Servicios
 Profile Owner    Overlay          Permisos
 DPC
       │               │                │
       └───────────────┼────────────────┘
                       ▼
                Evidence Model
                       │
                       ▼
             Resultado de la Fase 4
```

---

# 4. Arquitectura interna

Las responsabilidades deben permanecer separadas.

Arquitectura conceptual:

```text
DeviceControlAnalyzer
│
├── AdministrationAnalyzer
│   ├── DeviceAdminAnalyzer
│   ├── DeviceOwnerAnalyzer
│   ├── ProfileOwnerAnalyzer
│   └── DevicePolicyControllerAnalyzer
│
├── ControlCapabilityAnalyzer
│   ├── AccessibilityAnalyzer
│   ├── VpnAnalyzer
│   └── OverlayAnalyzer
│
├── PersistenceAnalyzer
│   ├── BootReceiverAnalyzer
│   └── PersistentServiceAnalyzer
│
├── AdministrativeCapabilityAnalyzer
│   ├── LockCapabilityAnalyzer
│   └── WipeCapabilityAnalyzer
│
├── IndicatorBuilder
│
└── DeviceControlEvidenceBuilder
```

No es obligatorio utilizar exactamente estos nombres o crear exactamente estas clases.

Sin embargo, deben mantenerse separadas las responsabilidades:

```text
ADMINISTRATION
      ≠
CONTROL CAPABILITIES
      ≠
PERSISTENCE
      ≠
INDICATOR INTERPRETATION
      ≠
RISK ASSESSMENT
```

No crear una única clase grande que concentre toda la lógica.

---

# 5. Estados de detección

Los indicadores deben utilizar estados explícitos.

Modelo base:

```text
CONFIRMED
ACTIVE
DECLARED
CAPABLE
DETECTED
NOT_DETECTED
NOT_ACCESSIBLE
NOT_AVAILABLE
NOT_DETERMINABLE
UNKNOWN
```

No todos los analizadores tienen que utilizar todos los estados.

Cada analizador debe utilizar únicamente los estados que tengan sentido técnico.

---

## 5.1 Significado de los estados

### CONFIRMED

Existe evidencia directa y verificable mediante una API o mecanismo disponible.

```text
Direct API result
System state
Verified configuration
```

---

### ACTIVE

La capacidad está confirmada como actualmente habilitada o en uso.

Ejemplo conceptual:

```text
Accessibility Service
        ↓
habilitado actualmente
        ↓
ACTIVE
```

---

### DECLARED

La aplicación declara explícitamente el componente o capacidad.

Ejemplo:

```text
AndroidManifest
        ↓
DeviceAdminReceiver
        ↓
DECLARED
```

---

### CAPABLE

La aplicación posee una combinación de componentes, permisos o configuración que indica que puede utilizar una capacidad.

Esto no implica que la capacidad esté activa.

---

### DETECTED

Existe evidencia de presencia de un indicador, pero no es posible confirmar completamente su estado.

---

### NOT_DETECTED

Se realizó una comprobación válida y no se encontró el indicador.

Este estado solamente debe utilizarse cuando la cobertura de la comprobación permita hacer esa afirmación.

---

### NOT_ACCESSIBLE

La API, permiso o restricción del sistema impide obtener el estado.

---

### NOT_AVAILABLE

La capacidad o API no está disponible en esa versión o configuración del dispositivo.

---

### NOT_DETERMINABLE

Los datos disponibles no permiten determinar el estado.

---

### UNKNOWN

El resultado es desconocido por una condición no clasificable en los estados anteriores.

---

# 6. Modelo unificado de indicador

Todos los analizadores deben converger hacia un modelo común.

Modelo conceptual:

```json
{
  "type": "DEVICE_ADMIN",
  "status": "DECLARED",
  "confidence": 0.98,
  "evidence": [
    {
      "type": "DEVICE_ADMIN_RECEIVER",
      "source": "MANIFEST",
      "value": "com.example.AdminReceiver"
    }
  ],
  "limitations": []
}
```

El modelo real puede adaptarse a la arquitectura existente.

Sin embargo, cada indicador debe permitir conocer:

```text
QUÉ se detectó
QUÉ estado tiene
POR QUÉ se llegó a esa conclusión
DE DÓNDE proviene la evidencia
QUÉ limitaciones existen
```

---

# 7. CHECKPOINT 4.1 — Arquitectura e integración

## Objetivo

Integrar la estructura de la Fase 4 sin implementar todavía todos los detectores.

## Implementar

* punto de entrada principal de la Fase 4;
* interfaces o modelos comunes necesarios;
* modelo de indicador;
* estados de detección;
* integración con el resultado de la Fase 3;
* mecanismo para agregar evidencias;
* mecanismo para registrar limitaciones.

## No implementar todavía

* Device Admin completo;
* Device Owner;
* Profile Owner;
* Accessibility;
* VPN;
* Overlay;
* persistencia;
* Risk Score.

## Resultado esperado

```text
NormalizedApp
        │
        ▼
DeviceControlAnalyzer
        │
        ▼
List<DeviceControlIndicator>
```

El proyecto debe compilar antes de continuar.

---

# 8. CHECKPOINT 4.2 — Device Administrator

## Objetivo

Detectar evidencia relacionada con `Device Administrator`.

## Analizar

Cuando los datos estén disponibles:

* receivers relacionados con Device Administration;
* declaración de `android.app.device_admin`;
* permisos o componentes relacionados;
* configuraciones obtenibles mediante APIs disponibles;
* evidencia directa cuando técnicamente sea posible;
* evidencia indirecta cuando la detección directa no sea posible.

## Regla crítica

Nunca concluir:

```text
DECLARED = ACTIVE
```

Debe distinguirse, como mínimo:

```text
DECLARED
ACTIVE
CONFIRMED
NOT_DETERMINABLE
NOT_ACCESSIBLE
```

según las capacidades reales de Android y la evidencia disponible.

## Resultado conceptual

```json
{
  "type": "DEVICE_ADMIN",
  "status": "DECLARED",
  "confidence": 0.98,
  "evidence": [
    {
      "type": "DEVICE_ADMIN_RECEIVER",
      "source": "PACKAGE_COMPONENT"
    }
  ]
}
```

---

# 9. CHECKPOINT 4.3 — Device Owner

## Objetivo

Analizar evidencia relacionada con `Device Owner`.

## Reglas

Utilizar APIs directas únicamente cuando estén disponibles para la aplicación.

Cuando no sea posible verificar directamente:

```text
NO afirmar que no existe.
```

Debe utilizarse:

```text
NOT_ACCESSIBLE
NOT_DETERMINABLE
```

según corresponda.

También pueden utilizarse indicadores indirectos, pero deben etiquetarse explícitamente como:

```text
INDIRECT_EVIDENCE
```

## Ejemplo conceptual

```json
{
  "type": "DEVICE_OWNER",
  "status": "NOT_DETERMINABLE",
  "confidence": 0.0,
  "evidence": [],
  "limitations": [
    "OWNER_STATE_NOT_ACCESSIBLE"
  ]
}
```

---

# 10. CHECKPOINT 4.4 — Profile Owner

## Objetivo

Aplicar la misma filosofía utilizada para Device Owner.

Analizar:

* APIs disponibles;
* configuración del sistema accesible;
* evidencia relacionada con perfiles administrados;
* posibles Device Policy Controllers.

Nunca confundir:

```text
No detectado
```

con:

```text
Confirmado como inexistente
```

si Android no permite realizar esa comprobación.

---

# 11. CHECKPOINT 4.5 — Device Policy Controller

## Objetivo

Detectar aplicaciones que actúan o podrían actuar como Device Policy Controller.

La detección debe combinar, cuando estén disponibles:

```text
Device Admin evidence
+
Owner/Profile evidence
+
Known package evidence
+
Administrative components
```

Una aplicación conocida de MDM o administración empresarial debe producir evidencia.

Sin embargo:

```text
KNOWN_MDM_PACKAGE
```

no significa automáticamente:

```text
ACTIVE_DEVICE_OWNER
```

Debe mantenerse la diferencia entre:

```text
IDENTIDAD DE LA APLICACIÓN
```

y:

```text
ESTADO ACTUAL DE ADMINISTRACIÓN
```

---

# 12. CHECKPOINT 4.6 — Accessibility

## Objetivo

Detectar aplicaciones que:

* declaran un Accessibility Service;
* tienen un servicio de accesibilidad identificable;
* se encuentran activos cuando el sistema permita comprobarlo.

Distinguir:

```text
DECLARED
```

de:

```text
ACTIVE
```

Ejemplo:

```text
AccessibilityService declarado
        ↓
DECLARED

AccessibilityService habilitado actualmente
        ↓
ACTIVE
```

Registrar siempre la evidencia utilizada.

---

# 13. CHECKPOINT 4.7 — VPN

## Objetivo

Detectar capacidades relacionadas con VPN.

Analizar:

* servicios relacionados con `VpnService`;
* permisos o declaraciones correspondientes;
* configuración activa cuando sea técnicamente accesible.

Distinguir:

```text
VPN_CAPABLE
```

de:

```text
VPN_ACTIVE
```

La presencia de una aplicación VPN no implica que exista una VPN activa.

---

# 14. CHECKPOINT 4.8 — Overlay

## Objetivo

Detectar capacidades relacionadas con superposición sobre otras aplicaciones.

Analizar evidencia relacionada con:

```text
SYSTEM_ALERT_WINDOW
```

u otros mecanismos relevantes disponibles.

Distinguir:

```text
PERMISSION_DECLARED
PERMISSION_GRANTED
ACTIVE
NOT_DETERMINABLE
```

cuando las APIs permitan establecer esas diferencias.

No asumir que una aplicación que declara el permiso está mostrando actualmente una superposición.

---

# 15. CHECKPOINT 4.9 — Persistencia

## Objetivo

Analizar mecanismos que pueden permitir que una aplicación vuelva a ejecutarse o permanezca activa.

Analizar:

### Boot Receivers

Indicadores como:

```text
BOOT_COMPLETED
LOCKED_BOOT_COMPLETED
```

Distinguir entre:

```text
RECEIVER_DECLARED
```

y:

```text
ACTUAL_EXECUTION
```

La Fase 4 normalmente solo debe afirmar la primera cuando no exista evidencia directa de ejecución.

---

### Servicios

Analizar servicios relevantes para:

* ejecución prolongada;
* foreground services;
* persistencia declarada;
* reinicio asociado a receivers.

No concluir que un servicio es "persistente" únicamente por existir.

La evidencia debe indicar exactamente:

```text
SERVICE_DECLARED
FOREGROUND_SERVICE_CAPABILITY
BOOT_TRIGGER
```

u otra condición concreta.

---

# 16. CHECKPOINT 4.10 — Capacidades administrativas

## Objetivo

Identificar evidencia relacionada con:

* bloqueo del dispositivo;
* `FORCE_LOCK`;
* wipe/reset administrativo;
* políticas relacionadas con administración.

## Regla crítica

No afirmar:

```text
La aplicación puede bloquear el dispositivo
```

si solamente existe un permiso indirectamente relacionado.

Distinguir entre:

```text
ADMINISTRATIVE_CAPABILITY_DECLARED
POLICY_RELATED_COMPONENT
DIRECT_API_CONFIRMATION
INDIRECT_EVIDENCE
NOT_DETERMINABLE
```

El resultado debe describir la capacidad técnica detectada, no una conclusión de riesgo.

---

# 17. CHECKPOINT 4.11 — Indicator Builder

## Objetivo

Unificar los resultados de todos los analizadores.

Entrada:

```text
Device Admin
Device Owner
Profile Owner
DPC
Accessibility
VPN
Overlay
Boot
Services
Administrative capabilities
```

Salida:

```text
List<DeviceControlIndicator>
```

Eliminar duplicados sin perder evidencia.

Si varias fuentes detectan el mismo indicador:

```text
Indicador
    │
    ├── evidencia 1
    ├── evidencia 2
    └── evidencia 3
```

No reemplazar una evidencia por otra.

---

# 18. CHECKPOINT 4.12 — Cobertura y limitaciones

La Fase 4 debe incluir información global sobre la cobertura del análisis.

Modelo conceptual:

```json
{
  "administrationCoverage": {
    "status": "PARTIAL",
    "analyzersExecuted": [
      "DEVICE_ADMIN",
      "ACCESSIBILITY",
      "VPN"
    ],
    "limitations": [
      "DEVICE_OWNER_STATE_NOT_ACCESSIBLE"
    ]
  }
}
```

Estados:

```text
COMPLETE
PARTIAL
LIMITED
UNKNOWN
```

No utilizar `COMPLETE` simplemente porque todos los analizadores se ejecutaron.

Debe representar la capacidad real de comprobación en ese dispositivo y versión de Android.

---

# 19. CHECKPOINT 4.13 — Salida integrada

El resultado final de la Fase 4 debe poder integrarse con el resultado de la Fase 3.

Modelo conceptual:

```json
{
  "applications": [
    {
      "packageName": "com.example.app",

      "classification": {
        "category": "USER",
        "confidence": 0.95,
        "evidence": []
      },

      "controlIndicators": [
        {
          "type": "ACCESSIBILITY",
          "status": "ACTIVE",
          "confidence": 1.0,
          "evidence": []
        },
        {
          "type": "BOOT_RECEIVER",
          "status": "DECLARED",
          "confidence": 0.98,
          "evidence": []
        }
      ]
    }
  ],

  "controlCoverage": {
    "status": "PARTIAL",
    "limitations": []
  }
}
```

La Fase 4 debe añadir información al modelo existente sin destruir:

* clasificación;
* evidencia de Fase 3;
* fuentes de descubrimiento;
* cobertura previa.

---

# 20. CHECKPOINT 4.14 — Pruebas

Las pruebas deben realizarse progresivamente.

Cada checkpoint debe seguir:

```text
IMPLEMENTAR
    ↓
COMPILAR
    ↓
PRUEBAS AUTOMATIZADAS
    ↓
PRUEBA EN DISPOSITIVO CUANDO APLIQUE
    ↓
VALIDAR RESULTADO
    ↓
DOCUMENTAR LIMITACIONES
```

Casos mínimos:

```text
1. Aplicación sin capacidades administrativas.
2. Aplicación con DeviceAdminReceiver declarado.
3. Aplicación con Accessibility Service declarado.
4. Accessibility Service activo, cuando exista.
5. Aplicación VPN.
6. VPN activa, cuando pueda comprobarse.
7. Aplicación con overlay.
8. Aplicación con BOOT_COMPLETED.
9. Servicio relevante.
10. Aplicación MDM/DPC conocida.
11. Caso donde Device Owner/Profile Owner no sea accesible.
12. Caso UNKNOWN o NOT_DETERMINABLE.
```

Si el dispositivo de prueba no contiene un caso:

```text
NO DISPONIBLE PARA PRUEBA FÍSICA
```

No inventar resultados.

Crear, cuando sea posible, fixtures o pruebas unitarias con datos simulados para validar la lógica de interpretación.

---

# 21. Lo que NO debe implementar la Fase 4

La Fase 4 no debe:

```text
❌ Asignar Risk Score.
❌ Clasificar como riesgo alto/medio/bajo.
❌ Concluir que una aplicación es maliciosa.
❌ Concluir que el teléfono está financiado.
❌ Concluir que el teléfono está bloqueado.
❌ Concluir que el teléfono está reportado como robado.
❌ Modificar políticas del dispositivo.
❌ Activar o desactivar administradores.
❌ Modificar Accessibility.
❌ Modificar VPN.
❌ Eliminar aplicaciones.
❌ Intentar evadir restricciones de Android.
```

La aplicación debe ser exclusivamente de análisis.

---

# 22. Regla Evidence-First

Toda conclusión de la Fase 4 debe poder responder:

```text
¿Qué se detectó?
```

```text
¿En qué estado?
```

```text
¿Qué evidencia lo respalda?
```

```text
¿La evidencia es directa o indirecta?
```

```text
¿Qué limitaciones existen?
```

Si una conclusión no puede responder estas preguntas, no debe generarse.

---

# 23. Formato obligatorio al finalizar cada checkpoint

Al terminar cada checkpoint, proporcionar:

```text
CHECKPOINT:
Estado: COMPLETADO / PARCIAL / BLOQUEADO

Objetivo:
- ...

Implementado:
- ...

Archivos modificados:
- ...

Archivos creados:
- ...

Integración con Fase 3:
- ...

APIs Android utilizadas:
- ...

Indicadores generados:
- ...

Evidencia utilizada:
- ...

Pruebas realizadas:
- ...

Resultado de compilación:
- ...

Limitaciones:
- ...

Decisiones técnicas:
- ...

Pendiente:
- ...

Siguiente checkpoint recomendado:
- ...
```

---

# 24. Criterio de finalización de la Fase 4

La Fase 4 estará completa cuando:

```text
✓ La arquitectura de análisis esté integrada con Fase 3.

✓ Los indicadores tengan estados explícitos.

✓ Cada indicador conserve su evidencia.

✓ Se distinga DECLARED de ACTIVE.

✓ Se distinga CAPABLE de CONFIRMED.

✓ Device Administrator sea analizado.

✓ Device Owner sea analizado dentro de los límites de Android.

✓ Profile Owner sea analizado dentro de los límites de Android.

✓ Device Policy Controller sea analizado.

✓ Accessibility sea analizado.

✓ VPN sea analizado.

✓ Overlay sea analizado.

✓ BOOT_COMPLETED y mecanismos relacionados sean analizados.

✓ Servicios relevantes sean analizados.

✓ Capacidades administrativas sean representadas como evidencia.

✓ Las limitaciones de cada API sean registradas.

✓ La cobertura global sea reportada.

✓ Los resultados se integren con Fase 3.

✓ No exista Risk Score en esta fase.

✓ No se generen conclusiones sobre financiación, bloqueo o robo.

✓ El proyecto compile correctamente.

✓ Las pruebas disponibles sean ejecutadas.
```

---

# 25. Instrucción inicial para el agente

Comienza exclusivamente con:

```text
CHECKPOINT 4.1 — Arquitectura e integración
```

Antes de modificar código:

1. inspecciona la implementación final de la Fase 3;
2. identifica el modelo exacto que produce;
3. identifica el punto correcto de integración;
4. revisa las pruebas existentes;
5. identifica APIs y utilidades ya implementadas que puedan reutilizarse.

Después presenta un diagnóstico breve:

```text
Arquitectura actual:
Resultado disponible de Fase 3:
Punto de integración:
Código reutilizable:
Modelo propuesto para Fase 4:
Archivos que deberán modificarse:
Riesgos técnicos:
Limitaciones de Android identificadas:
```

Solo después implementa el Checkpoint 4.1.

## Regla final

No implementar todos los checkpoints de una sola vez.

Cada checkpoint debe dejar el proyecto:

```text
COMPILANDO
+
FUNCIONAL
+
VALIDABLE
```

No avanzar automáticamente al siguiente checkpoint hasta que el checkpoint actual haya sido validado.


---

# 10. Fase 5 — Integridad del dispositivo

## Objetivo

Añadir señales de modificación del sistema.

### Investigar/detectar

- root
- bootloader
- Play Integrity
- build/test keys
- modificaciones conocidas
- aplicaciones de root
- estado de Verified Boot cuando sea accesible

### Importante

Una señal individual no debe considerarse prueba definitiva.

Ejemplo:

```text
user/test-keys
```

debe aparecer como:

```text
⚠ Indicador de configuración/build no estándar
```

y no automáticamente como:

```text
ROOT DETECTADO
```

---

# 11. Fase 6 — Interfaz

## Pantallas

### Inicio

```text
┌─────────────────────────┐
│   ANALIZAR EQUIPO       │
│                         │
│   Samsung A56           │
│                         │
│   Estado: Sin analizar  │
└─────────────────────────┘
```

### Análisis

Mostrar progreso:

```text
✓ Información del equipo
✓ Aplicaciones
✓ Permisos
✓ Administración
✓ Integridad
```

### Resultado

```text
RIESGO GENERAL

       🟠 ALTO

Motivos:
• Administrador activo
• FORCE_LOCK
• Aplicación asociada a plataforma conocida
```

### Detalle

Cada alerta debe permitir ver:

- aplicación
- package name
- indicador
- permiso/política
- evidencia
- nivel
- explicación
- fuente, cuando exista

---

# 12. Fase 7 — Informe

Crear informe exportable.

### Contenido

1. Identificación
2. Hardware
3. Software
4. Aplicaciones
5. Capacidades administrativas
6. Indicadores de financiación
7. Integridad
8. Riesgos
9. Recomendaciones
10. Fecha/hora
11. versión de la aplicación
12. versión de reglas

El informe debe diferenciar claramente:

```text
HECHO
INDICIO
NO DETERMINABLE
```

---

# 13. Fase 8 — Backend

Implementar cuando el MVP local esté funcionando.

### Servicios

```text
GET /rules/version
GET /apps/{package}
GET /device-models/{tac}
```

### Seguridad

- HTTPS
- autenticación cuando sea necesaria
- rate limiting
- logs mínimos
- anonimización cuando sea posible

---

# 14. Base de datos

Tablas iniciales:

```text
applications
application_certificates
application_indicators
device_models
tac_ranges
rule_versions
evidence_sources
```

### Principio

Toda regla externa debe ser versionada.

```text
rule_version
created_at
updated_at
source
confidence
```

---

# 15. Pruebas

Crear un laboratorio con dispositivos/emuladores que representen:

### Caso A

Teléfono normal sin software especial.

Esperado:

```text
LOW
```

### Caso B

Teléfono con Device Administrator legítimo.

Esperado:

```text
HIGH/RELEVANT
```

### Caso C

Teléfono administrado por MDM.

Esperado:

```text
HIGH
```

### Caso D

Aplicación financiera + capacidades administrativas.

Esperado:

```text
HIGH
```

---

# 16. Seguridad y privacidad

La aplicación puede manejar datos extremadamente sensibles del dispositivo.

### Reglas

- No enviar datos al backend por defecto.
- Mostrar qué datos serán enviados antes de una consulta externa.
- Cifrar información local sensible.
- No registrar números seriales en logs.
- Minimizar telemetría.
- Permitir eliminar el historial.
- No recolectar contenido personal de las aplicaciones.
- No inspeccionar mensajes, fotos, contactos ni archivos personales.

---

# 17. Compatibilidad con Google Play

Antes de publicar:

- Revisar política de visibilidad de paquetes.
- Justificar cualquier uso de `QUERY_ALL_PACKAGES`.
- Revisar permisos sensibles.
- Revisar APIs de administración.
- Revisar política de datos personales.
- Crear declaración de privacidad.
- Minimizar permisos.
- Evaluar si algunas funciones deben quedar en una versión distribuida fuera de Play.

No implementar funcionalidades únicamente porque técnicamente sean posibles si la distribución elegida no las permite.

---

# 18. MVP

La primera versión debe ser pequeña.

## MVP-1

**Esfuerzo estimado:** 3-4 semanas (1 desarrollador)

**Incluye:**

- Fase 0: Investigación técnica (3-5 días)
- Fase 1: Setup proyecto + Jest + Detox (2-3 días)
- Fase 2: DeviceInfoAnalyzer (2-3 días)
- Fase 3: PackageAnalyzer (4-5 días)
- Fase 4: DeviceAdminAnalyzer + detección indirecta (3-4 días)
- Fase 6: UI básica de resultados (3-4 días)
- Fase 7: Informe local (2-3 días)
- PoC en dispositivo real (2-3 días)

Implementar solamente:

```text
✓ Información del dispositivo
✓ Lista de aplicaciones visibles
✓ System/OEM/Google/User classification
✓ Package name
✓ Permisos
✓ Servicios
✓ Receivers
✓ Device Administrator
✓ Indicadores FORCE_LOCK
✓ Accessibility
✓ Device Owner/Profile Owner cuando sea accesible
✓ Informe local
```

No incluir inicialmente:

```text
✗ Backend complejo
✗ OCR
✗ Sistema avanzado de scoring
```

El objetivo del MVP es demostrar que el **análisis técnico local funciona correctamente**.

---

# 19. MVP-2

**Esfuerzo estimado:** 2-3 semanas (1 desarrollador)

Añadir:

```text
✓ Motor de reglas
✓ Risk Score
✓ Análisis de integridad
✓ Informe PDF/JSON
```

---

# 20. MVP-3

**Esfuerzo estimado:** 3-4 semanas (1-2 desarrolladores)

Añadir:

```text
✓ Backend
✓ OCR
✓ Historial de diagnósticos
✓ Comparación de equipos
```

---

# 21. Estructura de repositorio

```text
android-device-checker/
│
├── android/
│   └── app/
│       └── src/main/java/
│           └── com/devicechecker/
│               ├── PackageAnalyzer.kt
│               ├── PermissionAnalyzer.kt
│               ├── DeviceAdminAnalyzer.kt
│               ├── DevicePolicyAnalyzer.kt
│               ├── AccessibilityAnalyzer.kt
│               ├── SystemAppAnalyzer.kt
│               ├── IntegrityAnalyzer.kt
│               ├── TelephonyAnalyzer.kt
│               └── DeviceInfoAnalyzer.kt
│
├── src/
│   ├── components/
│   ├── screens/
│   ├── modules/
│   ├── domain/
│   ├── rules/
│   ├── services/
│   ├── storage/
│   ├── types/
│   └── utils/
│
├── docs/
│   ├── android-capabilities.md
│   ├── architecture.md
│   ├── privacy.md
│   └── threat-model.md
│
├── tests/
│
├── package.json
├── tsconfig.json
└── README.md
```

---

# 22. Orden recomendado de implementación

```text
1. Crear proyecto React Native
        ↓
2. Crear primer Turbo Native Module
        ↓
3. Obtener información del dispositivo
        ↓
4. Obtener aplicaciones
        ↓
5. Analizar permisos
        ↓
6. Analizar servicios/receivers
        ↓
7. Analizar Device Administrator
        ↓
8. Analizar Device Policy
        ↓
9. Analizar Accessibility
        ↓
10. Crear modelo de datos unificado
        ↓
11. Crear motor de reglas
        ↓
12. Crear UI de resultados
        ↓
13. Crear informe
        ↓
14. Añadir integridad
        ↓
15. Crear backend
        ↓
16. Pruebas con dispositivos reales
        ↓
17. Publicación
```

---

# 23. Criterios de éxito del MVP

El MVP se considera funcional cuando puede:

1. Identificar correctamente un dispositivo.
2. Enumerar las aplicaciones que Android permite visualizar.
3. Identificar package names.
4. Clasificar aplicaciones del sistema y terceros con una confianza explícita.
5. Mostrar permisos relevantes.
6. Detectar administradores activos cuando las APIs lo permitan.
7. Identificar indicadores de `FORCE_LOCK`.
8. Identificar servicios de accesibilidad relevantes.
9. Detectar Device Owner/Profile Owner cuando sea posible.
10. Mostrar claramente las limitaciones de Android.
11. Generar un informe reproducible con evidencias.
12. No modificar ni bloquear el dispositivo analizado.

---

# 24. Regla fundamental del producto

El objetivo de la aplicación no debe ser:

> "Encontrar una app de una financiera."

Debe ser:

> **"Determinar qué mecanismos de administración, restricción o control existen en el dispositivo y evaluar si existen indicadores compatibles con financiación o bloqueo."**

Esta diferencia será fundamental para que el diagnóstico sea técnicamente sólido y evite conclusiones incorrectas.

---

# 25. Próximo paso

Antes de escribir una gran cantidad de código, realizar una **Prueba de Concepto (PoC)** de 2 módulos:

**Esfuerzo estimado PoC:** 3-5 días (1 desarrollador)

```text
PoC 1
PackageAnalyzer
    ↓
Lista completa de información disponible
de las aplicaciones.

PoC 2
DeviceAdminAnalyzer
    ↓
Detectar administradores y capacidades
administrativas disponibles (directa e indirectamente).

```

Si estas dos PoC funcionan correctamente en un dispositivo físico, construir el resto del MVP sobre ellas.

---

# 26. Resultado final esperado

La aplicación debe terminar ofreciendo un diagnóstico como:

```text
════════════════════════════════════
       INFORME DE DIAGNÓSTICO
════════════════════════════════════

DISPOSITIVO
Samsung Galaxy A56 5G
Android 16

INTEGRIDAD
✓ Sin indicadores concluyentes de root
⚠ Build/configuración no estándar detectada

ADMINISTRACIÓN
🔴 Device Administrator detectado
🔴 FORCE_LOCK disponible

APLICACIONES
124 analizadas
98 sistema/OEM
18 Google
8 terceros relevantes

Aplicación:
com.example.devicecontrol

Evidencia:
• Administrador activo
• FORCE_LOCK
• Servicio persistente
• Asociación conocida con plataforma

CONCLUSIÓN
🟠 RIESGO ELEVADO

La información disponible presenta indicadores
compatibles con administración
del dispositivo. Se recomienda verificar con el
vendedor la liberación/cancelación del equipo.

════════════════════════════════════
```

La conclusión **nunca debe decir "equipo robado" o "equipo financiado" sin una fuente que permita establecerlo**.
